class JT
  class Docker
    include Utilities

    def docker(*args)
      command = args.shift
      case command
      when 'build'
        docker_build(*args)
      when nil, 'test'
        docker_test(*args)
      when 'print'
        puts dockerfile(*args)
      else
        abort "Unkown jt docker command #{command}"
      end
    end

    private def docker_build(*args)
      if args.first.nil? || args.first.start_with?('--')
        image_name = 'truffleruby-test'
      else
        image_name = args.shift
      end
      docker_dir = File.join(TRUFFLERUBY_DIR, 'tool', 'docker')
      File.write(File.join(docker_dir, 'Dockerfile'), dockerfile(*args))
      sh 'docker', 'build', '-t', image_name, '.', chdir: docker_dir
    end

    private def docker_test(*args)
      distros = ['--ol7', '--ubuntu1804', '--ubuntu1604', '--fedora28']

      distros.each do |distro|
        puts '**********************************'
        puts '**********************************'
        puts '**********************************'
        distros.each do |d|
          print d
          print '     <---' if d == distro
          puts
        end
        puts '**********************************'
        puts '**********************************'
        puts '**********************************'

        docker 'build', distro, *args
      end
    end

    private def dockerfile(*args)
      config = @config ||= YAML.load_file(File.join(TRUFFLERUBY_DIR, 'tool', 'docker-configs.yaml'))

      truffleruby_repo = 'https://github.com/oracle/truffleruby.git'
      distro = 'ol7'
      install_method = :public
      public_version = '1.0.0-rc14'
      rebuild_images = false
      rebuild_openssl = true
      basic_test = false
      full_test = false
      root = false

      until args.empty?
        arg = args.shift
        case arg
        when '--repo'
          truffleruby_repo = args.shift
        when '--ol7', '--ubuntu1804', '--ubuntu1604', '--fedora28'
          distro = arg[2..-1]
        when '--public'
          install_method = :public
          public_version = args.shift
        when '--graalvm'
          install_method = :graalvm
          graalvm_tarball = args.shift
          graalvm_component = args.shift
        when '--standalone'
          install_method = :standalone
          standalone_tarball = args.shift
        when '--source'
          install_method = :source
          source_branch = args.shift
        when '--rebuild-images'
          rebuild_images = true
          native_component = args.shift
        when '--no-rebuild-openssl'
          rebuild_openssl = false
        when '--basic-test'
          basic_test = true
        when '--test'
          full_test = true
          test_branch = args.shift
        when '--root'
          root = true
        else
          abort "unknown option #{arg}"
        end
      end

      distro = config.fetch(distro)
      run_post_install_hook = rebuild_openssl

      lines = []
      packages = []

      lines << "FROM #{distro.fetch('base')}"
      lines.push(*distro.fetch('setup'))
      lines.push(*distro.fetch('locale'))

      packages << distro.fetch('curl') if install_method == :public
      packages << distro.fetch('git') if install_method == :source || full_test
      packages << distro.fetch('which') if full_test
      packages << distro.fetch('find') if full_test
      packages << distro.fetch('source') if install_method == :source
      packages << distro.fetch('images') if rebuild_images

      packages << distro.fetch('zlib')
      packages << distro.fetch('openssl')
      packages << distro.fetch('cext')

      lines << [distro.fetch('install'), *packages.compact].join(' ')

      lines << 'WORKDIR /test'

      lines << 'RUN useradd -ms /bin/bash test'
      lines << 'RUN chown test /test'
      lines << 'USER test' unless root

      docker_dir = File.join(TRUFFLERUBY_DIR, 'tool', 'docker')

      check_post_install_message = [
          "RUN grep 'The Ruby openssl C extension needs to be recompiled on your system to work with the installed libssl' install.log",
          "RUN grep '/#{language_dir}/ruby/lib/truffle/post_install_hook.sh' install.log"
      ]

      case install_method
      when :public
        graalvm_tarball = "graalvm-ce-#{public_version}-linux-amd64.tar.gz"
        lines << "RUN curl -OL https://github.com/oracle/graal/releases/download/vm-#{public_version}/#{graalvm_tarball}"
        graalvm_base = '/test/graalvm'
        lines << "RUN mkdir #{graalvm_base}"
        lines << "RUN tar -zxf #{graalvm_tarball} -C #{graalvm_base} --strip-components=1"
        lines << "RUN #{graalvm_base}/bin/gu install org.graalvm.ruby | tee install.log"
        lines.push(*check_post_install_message)
        ruby_base = "#{graalvm_base}/#{language_dir}/ruby"
        graalvm_bin = "#{graalvm_base}/bin"
        ruby_bin = graalvm_bin
        lines << "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
      when :graalvm
        FileUtils.copy graalvm_tarball, docker_dir
        FileUtils.copy graalvm_component, docker_dir
        graalvm_tarball = File.basename(graalvm_tarball)
        graalvm_component = File.basename(graalvm_component)
        lines << "COPY #{graalvm_tarball} /test/"
        lines << "COPY #{graalvm_component} /test/"
        graalvm_base = '/test/graalvm'
        lines << "RUN mkdir #{graalvm_base}"
        lines << "RUN tar -zxf #{graalvm_tarball} -C #{graalvm_base} --strip-components=1"
        ruby_base = "#{graalvm_base}/#{language_dir}/ruby"
        graalvm_bin = "#{graalvm_base}/bin"
        ruby_bin = graalvm_bin
        lines << "RUN #{graalvm_bin}/gu install --file /test/#{graalvm_component} | tee install.log"
        lines.push(*check_post_install_message)
        lines << "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
      when :standalone
        FileUtils.copy standalone_tarball, docker_dir
        standalone_tarball = File.basename(standalone_tarball)
        lines << "COPY #{standalone_tarball} /test/"
        ruby_base = '/test/truffleruby-standalone'
        lines << "RUN mkdir #{ruby_base}"
        lines << "RUN tar -zxf #{standalone_tarball} -C #{ruby_base} --strip-components=1"
        ruby_bin = "#{ruby_base}/bin"
        lines << "RUN #{ruby_base}/lib/truffle/post_install_hook.sh" if run_post_install_hook
      when :source
        lines << 'RUN git clone --depth 1 https://github.com/graalvm/mx.git'
        lines << 'ENV PATH=$PATH:/test/mx'
        lines << 'RUN git clone --depth 1 https://github.com/graalvm/graal-jvmci-8.git'

        # Disable compiler warnings as errors, as we may be using a more recent compiler
        lines << "RUN sed -i 's/WARNINGS_ARE_ERRORS = -Werror/WARNINGS_ARE_ERRORS = /g' graal-jvmci-8/make/linux/makefiles/gcc.make"

        lines << 'RUN cd graal-jvmci-8 && JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac)))) mx build'
        lines << "ENV JAVA_HOME=/test/graal-jvmci-8/#{distro.fetch('jdk')}/linux-amd64/product"
        lines << 'ENV PATH=$JAVA_HOME/bin:$PATH'
        lines << 'ENV JVMCI_VERSION_CHECK=ignore'
        lines << 'RUN java -version'
        lines << 'RUN git clone https://github.com/oracle/graal.git'
        lines << "RUN git clone --depth 1 --branch #{source_branch} #{truffleruby_repo}"
        lines << 'RUN cd truffleruby && mx build'
        lines << 'RUN cd graal/compiler && mx build'
        lines << "ENV JAVA_OPTS='-XX:+UnlockExperimentalVMOptions -XX:+EnableJVMCI -Djvmci.class.path.append=/test/graal/compiler/mxbuild/dists/jdk1.8/graal.jar'"
        ruby_base = '/test/truffleruby'
        ruby_bin = "#{ruby_base}/bin"
      end

      if full_test
        test_files = %w[
          spec
          test/truffle/compiler/pe
          versions.json
        ]

        chdir(docker_dir) do
          FileUtils.rm_rf 'truffleruby-tests'
          raw_sh 'git', 'clone', '--branch', test_branch, TRUFFLERUBY_DIR, 'truffleruby-tests'
          test_files.each do |file|
            FileUtils.cp_r "truffleruby-tests/#{file}", '.'
          end
          FileUtils.rm_rf 'truffleruby-tests'
        end
      end


      if rebuild_images
        if [:public, :graalvm].include?(install_method)
          FileUtils.copy native_component, docker_dir
          native_component = File.basename(native_component)
          lines << "COPY #{native_component} /test/"
          lines << "RUN #{graalvm_bin}/gu install --file /test/#{native_component} | tee install.log"
          lines << "RUN #{graalvm_base}/bin/gu rebuild-images polyglot libpolyglot"
        else
          abort "can't rebuild images for a build not from public or from local GraalVM components"
        end
      end

      lines << "ENV PATH=#{ruby_bin}:$PATH"

      configs = [:public, :graalvm].include?(install_method) ? %w[--native --jvm] : ['']

      configs.each do |c|
        lines << "RUN ruby #{c} --version"
      end

      if basic_test || full_test
        configs.each do |c|
          lines << "RUN cp -r #{ruby_base}/lib/gems /test/clean-gems"

          gem_install = "ruby #{c} -S gem install --no-document"
          lines << "RUN #{gem_install} color"
          lines << "RUN ruby #{c} -rcolor -e 'raise unless defined?(Color)'"

          lines << "RUN #{gem_install} oily_png"
          lines << "RUN ruby #{c} -roily_png -e 'raise unless defined?(OilyPNG::Color)'"

          lines << "RUN #{gem_install} unf"
          lines << "RUN ruby #{c} -runf -e 'raise unless defined?(UNF)'"

          lines << "RUN rm -rf #{ruby_base}/lib/gems"
          lines << "RUN mv /test/clean-gems #{ruby_base}/lib/gems"
        end
      end

      if full_test
        test_files.each do |path|
          file = File.basename(path)
          lines << "COPY --chown=test #{file} #{file}"
        end

        configs.each do |c|
          excludes = ['fails', 'slow']

          [':command_line', ':security', ':language', ':core', ':library', ':capi', ':library_cext', ':truffle', ':truffle_capi'].each do |set|
            t_config = c.empty? ? '' : '-T' + c
            t_excludes = excludes.map { |e| '--excl-tag ' + e }.join(' ')
            lines << "RUN ruby spec/mspec/bin/mspec --config spec/truffle.mspec -t #{ruby_bin}/ruby #{t_config} #{t_excludes} #{set}"
          end
        end

        configs.each do |c|
          lines << "RUN ruby #{c} --vm.Dgraal.TruffleCompilationExceptionsAreThrown=true --vm.Dgraal.TruffleIterativePartialEscape=true pe/pe.rb || true"
        end
      end

      lines << 'CMD bash'

      lines.join("\n") + "\n"
    end
  end
end
