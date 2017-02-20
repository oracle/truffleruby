# TruffleRuby: Shell to gunzip in bundler updater

class Bundler::CompactIndexClient
  class Updater
    def update(local_path, remote_path, retrying = nil)
      headers = {}

      Dir.mktmpdir(local_path.basename.to_s, local_path.dirname) do |local_temp_dir|

        local_temp_path = Pathname.new(local_temp_dir).join(local_path.basename)


        # download new file if retrying
        if retrying.nil? && local_path.file?
          FileUtils.cp local_path, local_temp_path
          headers["If-None-Match"] = etag_for(local_temp_path)
          headers["Range"] = "bytes=#{local_temp_path.size}-"
        else
          # Fastly ignores Range when Accept-Encoding: gzip is set
          headers["Accept-Encoding"] = "gzip"
        end

        response = @fetcher.call(remote_path, headers)
        return if response.is_a?(Net::HTTPNotModified)

        content = response.body

        if response["Content-Encoding"] == "gzip"
          IO.binwrite("#{local_temp_dir}/gzipped_versions", content)

          #content = Zlib::GzipReader.new(StringIO.new(content)).read
          content = `gunzip -c #{local_temp_dir}/gzipped_versions`
        end

        mode = response.is_a?(Net::HTTPPartialContent) ? "a" : "w"
        local_temp_path.open(mode) { |f| f << content }


        response_etag = response["ETag"]

        if etag_for(local_temp_path) == response_etag
          FileUtils.mv(local_temp_path, local_path)
          return
        end

        if retrying.nil?
          update(local_path, remote_path, :retrying)
        else
          # puts "ERROR: #{remote_path},#{local_path},#{local_temp_path}"
          raise MisMatchedChecksumError.new(remote_path, response_etag, etag_for(local_temp_path))
        end
      end
    end
  end
end
