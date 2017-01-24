version = '9.1.7.0'

project 'JRuby', 'https://github.com/jruby/jruby' do

  model_version '4.0.0'
  inception_year '2001'
  id 'org.jruby:jruby-parent', version
  inherit 'org.sonatype.oss:oss-parent:7'
  packaging 'pom'

  description 'JRuby is the effort to recreate the Ruby (http://www.ruby-lang.org) interpreter in Java.'

  organization 'JRuby', 'http://jruby.org'

  [ 'headius', 'enebo', 'wmeissner', 'BanzaiMan', 'mkristian' ].each do |name|
    developer name do
      name name
      roles 'developer'
    end
  end

  license 'GPL 3', 'http://www.gnu.org/licenses/gpl-3.0-standalone.html'
  license 'LGPL 3', 'http://www.gnu.org/licenses/lgpl-3.0-standalone.html'
  license 'EPL', 'http://www.eclipse.org/legal/epl-v10.html'

  plugin_repository( :url => 'https://oss.sonatype.org/content/repositories/snapshots/',
                     :id => 'sonatype' ) do
    releases 'false'
    snapshots 'true'
  end
  repository( :url => 'https://oss.sonatype.org/content/repositories/snapshots/',
              :id => 'sonatype' ) do
    releases 'false'
    snapshots 'true'
  end

  properties( 'its.j2ee' => 'j2ee*/pom.xml',
              'its.osgi' => 'osgi*/pom.xml',
              'jruby.basedir' => '${project.basedir}',
              'main.basedir' => '${project.basedir}',
              'project.build.sourceEncoding' => 'utf-8',
              'base.java.version' => '1.7',
              'base.javac.version' => '1.7',
              'invoker.skip' => 'true',
              'version.jruby' => '${project.version}',
              'github.global.server' => 'github',
              'polyglot.dump.pom' => 'pom.xml',
              'polyglot.dump.readonly' => 'true',
              'jruby.plugins.version' => '1.0.10',

              'json.version' => '1.8.3',
              'rspec.version' => '3.4.0',
              'rspec-core.version' => '3.4.4',
              'rspec-expectations.version' => '3.4.0',
              'rspec-mocks.version' => '3.4.1',
              'rspec-support.version' => '3.4.1',
              'minitest.version' => '5.4.1',
              'test-unit.version' => '3.1.1',
              'power_assert.version' => '0.2.3',
              'diff-lcs.version' => '1.1.3',
              'racc.version' => '1.4.14',
              # versions for default gems with bin executables
              # used in ./lib/pom.rb and ./maven/jruby-stdlib/pom.rb
              'rdoc.version' => '4.2.0',
              'rake.version' => '10.4.2',
              'jar-dependencies.version' => '0.3.9',

              'jruby-launcher.version' => '1.1.1',
              'ant.version' => '1.9.2',
              'asm.version' => '5.0.4',
              'jffi.version' => '1.2.14',
              'bouncy-castle.version' => '1.47',
              'joda.time.version' => '2.8.2' )

  plugin_management do
    jar( 'junit:junit:4.11',
         :scope => 'test' )

    plugin( 'org.apache.felix:maven-bundle-plugin:2.4.0',
            'instructions' => {
              'Export-Package' =>  'org.jruby.*;version=${project.version}',
              'Import-Package' =>  '!org.jruby.*, *;resolution:=optional',
              'Private-Package' =>  'org.jruby.*,jnr.*,com.kenai.*,com.martiansoftware.*,jay.*,jline.*,jni.*,org.fusesource.*,org.jcodings.*,org.joda.convert.*,org.joda.time.*,org.joni.*,org.yaml.*,org.yecht.*,tables.*,org.objectweb.*,com.headius.*,org.bouncycastle.*,com.jcraft.jzlib,.',
              'Bundle-Name' =>  '${bundle.name} ${project.version}',
              'Bundle-Description' =>  '${bundle.name} ${project.version} OSGi bundle',
              'Bundle-SymbolicName' =>  '${bundle.symbolic_name}'
            } ) do
      execute_goals( 'manifest',
                     :phase => 'prepare-package' )
    end

    plugin( :site, '3.3', 'skipDeploy' =>  'true' )
    plugin 'org.codehaus.mojo:build-helper-maven-plugin:1.8'
    plugin 'org.codehaus.mojo:exec-maven-plugin:1.2.1'
    plugin :antrun, '1.7'
    plugin :source, '2.1.2'
    plugin :assembly, '2.4'
    plugin :install, '2.4'
    plugin :deploy, '2.7'
    plugin :javadoc, '2.7'
    plugin :resources, '2.6'
    plugin :clean, '2.5'
    plugin :dependency, '2.8'
    plugin :release, '2.4.1'
    plugin :jar, '2.6'

    rules = { :requireMavenVersion => { :version => '[3.3.0,)' } }
    # unless model.version =~ /-SNAPSHOT/
    #    rules[:requireReleaseDeps] = { :message => 'No Snapshots Allowed!' }
    # end
    plugin :enforcer, '1.4' do
      execute_goal :enforce, :rules => rules
    end

    plugin :compiler, '3.3'
    plugin :shade, '2.4.3'
    plugin :surefire, '2.15'
    plugin :plugin, '3.2'

  end

  modules [ 'truffle' ]

  build do
    default_goal 'install'
  end

  profile 'test' do
    properties 'invoker.skip' => false
    modules [ 'test' ]
  end

  all_modules = [ 'truffle', 'test' ]

end
