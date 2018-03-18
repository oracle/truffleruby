# frozen_string_literal: true

# Portions copyright (c) 2010 Andre Arko
# Portions copyright (c) 2009 Engine Yard
# 
# MIT License
# 
# Permission is hereby granted, free of charge, to any person obtaining a copy
# of this software and associated documentation files (the "Software"), to deal
# in the Software without restriction, including without limitation the rights
# to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
# copies of the Software, and to permit persons to whom the Software is
# furnished to do so, subject to the following conditions:
# 
# The above copyright notice and this permission notice shall be included in all
# copies or substantial portions of the Software.
# 
# THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
# IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
# FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
# AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
# LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
# OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
# SOFTWARE.

Truffle::Patching.require_original __FILE__

# TruffleRuby: shell to gunzip for unzipping

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
