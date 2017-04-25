# frozen_string_literal: true

Truffle::Patching.require_original __FILE__

require "fileutils"
require "stringio"
require "tmpdir"
require "zlib"

module Bundler
  class CompactIndexClient
    class Updater
      def update(local_path, remote_path, retrying = nil)
        headers = {}

        Dir.mktmpdir("bundler-compact-index-") do |local_temp_dir|
          local_temp_path = Pathname.new(local_temp_dir).join(local_path.basename)

          # first try to fetch any new bytes on the existing file
          if retrying.nil? && local_path.file?
            FileUtils.cp local_path, local_temp_path
            headers["If-None-Match"] = etag_for(local_temp_path)
            headers["Range"] = "bytes=#{local_temp_path.size}-"
          else
            # Fastly ignores Range when Accept-Encoding: gzip is set
            headers["Accept-Encoding"] = "gzip"
          end

          response = @fetcher.call(remote_path, headers)
          return nil if response.is_a?(Net::HTTPNotModified)

          content = response.body
          if response["Content-Encoding"] == "gzip"
            # content = Zlib::GzipReader.new(StringIO.new(content)).read
            IO.binwrite("#{local_temp_dir}/gzipped_versions", content)
            content = `gunzip -c #{local_temp_dir}/gzipped_versions`
          end

          mode = response.is_a?(Net::HTTPPartialContent) ? "a" : "w"
          SharedHelpers.filesystem_access(local_temp_path) do
            local_temp_path.open(mode) {|f| f << content }
          end

          response_etag = (response["ETag"] || "").gsub(%r{\AW/}, "")
          if etag_for(local_temp_path) == response_etag
            SharedHelpers.filesystem_access(local_path) do
              FileUtils.mv(local_temp_path, local_path)
            end
            return nil
          end

          if retrying
            raise MisMatchedChecksumError.new(remote_path, response_etag, etag_for(local_temp_path))
          end

          update(local_path, remote_path, :retrying)
        end
      end
    end
  end
end
