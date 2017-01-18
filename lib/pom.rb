MORE_QUIET = ENV['JRUBY_BUILD_MORE_QUIET']

if MORE_QUIET
  class Gem::Installer
    def say(message)
      if message != spec.post_install_message || !MORE_QUIET
        super
      end
    end
  end
end

def log(message=nil)
  puts message unless MORE_QUIET
end

project 'JRuby Lib Setup' do

  version = ENV['JRUBY_VERSION'] ||
    File.read( File.join( basedir, '..', 'VERSION' ) ).strip

  model_version '4.0.0'
  id 'jruby-stdlib'
  inherit "org.jruby:jruby-parent", version

  properties( 'polyglot.dump.pom' => 'pom.xml',
              'polyglot.dump.readonly' => true,
              'jruby.plugins.version' => '1.1.2',
              'gem.home' => '${basedir}/ruby/gems/shared',
              # we copy everything into the target/classes/META-INF
              # so the jar plugin just packs it - see build/resources below
              'jruby.complete.home' => '${project.build.outputDirectory}/META-INF/jruby.home',
              'jruby.complete.gems' => '${jruby.complete.home}/lib/ruby/gems/shared' )

  # just depends on jruby-core so we are sure the jruby.jar is in place
  jar "org.jruby:jruby-core:#{version}", :scope => 'test'

  extension 'org.torquebox.mojo:mavengem-wagon:0.2.0'

  repository :id => :mavengems, :url => 'mavengem:https://rubygems.org'

  # for testing out jruby-ossl before final release :
  #repository( :url => 'https://oss.sonatype.org/content/repositories/snapshots',
  #            :id => 'gem-snaphots' )
  #repository( :url => 'http://oss.sonatype.org/content/repositories/staging',
  #            :id => 'gem-staging' )

  plugin( :clean,
          :filesets => [ { :directory => '${basedir}/ruby/gems/shared/specifications/default',
                           :includes => [ '*' ] },
                         { :directory => '${basedir}/ruby/stdlib',
                           :includes => [ 'org/**/*.jar' ] } ] )

  # we have no sources and attach an empty jar later in the build to
  # satisfy oss.sonatype.org upload

  plugin( :source, 'skipSource' => 'true' )

  # this plugin is configured to attach empty jars for sources and javadocs
  plugin( 'org.codehaus.mojo:build-helper-maven-plugin' )

  build do

    # both resources are includes for the $jruby_home/lib directory

    resource do
      directory '${gem.home}'
      includes 'gems/rake-${rake.version}/bin/r*', 'gems/rdoc-${rdoc.version}/bin/r*', 'specifications/default/*.gemspec'
      target_path '${jruby.complete.gems}'
    end

    resource do
      directory '${basedir}/..'
      includes 'bin/ast*', 'bin/gem*', 'bin/irb*', 'bin/jgem*', 'bin/jirb*', 'bin/jruby*', 'bin/rake*', 'bin/ri*', 'bin/rdoc*', 'bin/testrb*', 'lib/ruby/include/**', 'lib/ruby/stdlib/**', 'lib/ruby/truffle/**'
      excludes 'bin/jruby', 'bin/jruby*_*', 'bin/jruby*-*', '**/.*',
        'lib/ruby/stdlib/rubygems/defaults/jruby_native.rb',
        'lib/ruby/stdlib/gauntlet*.rb' # gauntlet_rdoc.rb, gauntlet_rubygems.rb
      target_path '${jruby.complete.home}'
    end

    resource do
      directory '${project.basedir}/..'
      includes [ 'BSDL', 'COPYING', 'LEGAL', 'LICENSE.RUBY' ]
      target_path '${project.build.outputDirectory}/META-INF/'
    end
  end
end
