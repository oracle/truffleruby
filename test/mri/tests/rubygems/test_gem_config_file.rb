# frozen_string_literal: true

require_relative "helper"
require "rubygems/config_file"

class TestGemConfigFile < Gem::TestCase
  def setup
    super

    credential_setup

    @temp_conf = File.join @tempdir, ".gemrc"

    @cfg_args = %W[--config-file #{@temp_conf}]

    Gem::ConfigFile::OPERATING_SYSTEM_DEFAULTS.clear
    Gem::ConfigFile::PLATFORM_DEFAULTS.clear

    @env_gemrc = ENV["GEMRC"]
    ENV["GEMRC"] = ""

    util_config_file
  end

  def teardown
    Gem::ConfigFile::OPERATING_SYSTEM_DEFAULTS.clear
    Gem::ConfigFile::PLATFORM_DEFAULTS.clear

    ENV["GEMRC"] = @env_gemrc

    credential_teardown

    super
  end

  def test_initialize
    assert_equal @temp_conf, @cfg.config_file_name

    assert_equal true, @cfg.backtrace
    assert_equal true, @cfg.update_sources
    assert_equal Gem::ConfigFile::DEFAULT_BULK_THRESHOLD, @cfg.bulk_threshold
    assert_equal true, @cfg.verbose
    assert_equal [@gem_repo], Gem.sources
    assert_equal 365, @cfg.cert_expiration_length_days
    assert_equal false, @cfg.ipv4_fallback_enabled

    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: true"
      fp.puts ":update_sources: false"
      fp.puts ":bulk_threshold: 10"
      fp.puts ":verbose: false"
      fp.puts ":sources:"
      fp.puts "  - http://more-gems.example.com"
      fp.puts "install: --wrappers"
      fp.puts ":gempath:"
      fp.puts "- /usr/ruby/1.8/lib/ruby/gems/1.8"
      fp.puts "- /var/ruby/1.8/gem_home"
      fp.puts ":ssl_verify_mode: 0"
      fp.puts ":ssl_ca_cert: /etc/ssl/certs"
      fp.puts ":cert_expiration_length_days: 28"
      fp.puts ":ipv4_fallback_enabled: true"
    end

    util_config_file
    assert_equal true, @cfg.backtrace
    assert_equal 10, @cfg.bulk_threshold
    assert_equal false, @cfg.verbose
    assert_equal false, @cfg.update_sources
    assert_equal %w[http://more-gems.example.com], @cfg.sources
    assert_equal "--wrappers", @cfg[:install]
    assert_equal(["/usr/ruby/1.8/lib/ruby/gems/1.8", "/var/ruby/1.8/gem_home"],
                 @cfg.path)
    assert_equal 0, @cfg.ssl_verify_mode
    assert_equal "/etc/ssl/certs", @cfg.ssl_ca_cert
    assert_equal 28, @cfg.cert_expiration_length_days
    assert_equal true, @cfg.ipv4_fallback_enabled
  end

  def test_initialize_ipv4_fallback_enabled_env
    ENV["IPV4_FALLBACK_ENABLED"] = "true"
    util_config_file %W[--config-file #{@temp_conf}]

    assert_equal true, @cfg.ipv4_fallback_enabled
  end

  def test_initialize_handle_arguments_config_file
    util_config_file %W[--config-file #{@temp_conf}]

    assert_equal @temp_conf, @cfg.config_file_name
  end

  def test_initialize_handle_arguments_config_file_with_other_params
    util_config_file %W[--config-file #{@temp_conf} --backtrace]

    assert_equal @temp_conf, @cfg.config_file_name
  end

  def test_initialize_handle_arguments_config_file_equals
    util_config_file %W[--config-file=#{@temp_conf}]

    assert_equal @temp_conf, @cfg.config_file_name
  end

  def test_initialize_operating_system_override
    Gem::ConfigFile::OPERATING_SYSTEM_DEFAULTS[:bulk_threshold] = 1
    Gem::ConfigFile::OPERATING_SYSTEM_DEFAULTS["install"] = "--no-env-shebang"

    Gem::ConfigFile::PLATFORM_DEFAULTS[:bulk_threshold] = 2

    util_config_file

    assert_equal 2, @cfg.bulk_threshold
    assert_equal "--no-env-shebang", @cfg[:install]
  end

  def test_initialize_platform_override
    Gem::ConfigFile::PLATFORM_DEFAULTS[:bulk_threshold] = 2
    Gem::ConfigFile::PLATFORM_DEFAULTS["install"] = "--no-env-shebang"

    File.open Gem::ConfigFile::SYSTEM_WIDE_CONFIG_FILE, "w" do |fp|
      fp.puts ":bulk_threshold: 3"
    end

    util_config_file

    assert_equal 3, @cfg.bulk_threshold
    assert_equal "--no-env-shebang", @cfg[:install]
  end

  def test_initialize_system_wide_override
    File.open Gem::ConfigFile::SYSTEM_WIDE_CONFIG_FILE, "w" do |fp|
      fp.puts ":backtrace: false"
      fp.puts ":bulk_threshold: 2048"
    end

    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: true"
    end

    util_config_file

    assert_equal 2048, @cfg.bulk_threshold
    assert_equal true, @cfg.backtrace
  end

  def test_initialize_environment_variable_override
    File.open Gem::ConfigFile::SYSTEM_WIDE_CONFIG_FILE, "w" do |fp|
      fp.puts ":backtrace: false"
      fp.puts ":verbose: false"
      fp.puts ":bulk_threshold: 2048"
    end

    conf1 = File.join @tempdir, "gemrc1"
    File.open conf1, "w" do |fp|
      fp.puts ":backtrace: true"
    end

    conf2 = File.join @tempdir, "gemrc2"
    File.open conf2, "w" do |fp|
      fp.puts ":verbose: true"
    end

    conf3 = File.join @tempdir, "gemrc3"
    File.open conf3, "w" do |fp|
      fp.puts ":verbose: :loud"
    end
    ps = File::PATH_SEPARATOR
    ENV["GEMRC"] = conf1 + ps + conf2 + ps + conf3

    util_config_file

    assert_equal true, @cfg.backtrace
    assert_equal :loud, @cfg.verbose
    assert_equal 2048, @cfg.bulk_threshold
  end

  def test_set_config_file_name_from_environment_variable
    ENV["GEMRC"] = "/tmp/.gemrc"
    cfg = Gem::ConfigFile.new([])
    assert_equal cfg.config_file_name, "/tmp/.gemrc"
  end

  def test_api_keys
    assert_nil @cfg.instance_variable_get :@api_keys

    temp_cred = File.join Gem.user_home, ".gem", "credentials"
    FileUtils.mkdir_p File.dirname(temp_cred)
    File.open temp_cred, "w", 0o600 do |fp|
      fp.puts ":rubygems_api_key: 701229f217cdf23b1344c7b4b54ca97"
    end

    util_config_file

    assert_equal({ rubygems: "701229f217cdf23b1344c7b4b54ca97" },
                 @cfg.api_keys)
  end

  def test_check_credentials_permissions
    pend "chmod not supported" if Gem.win_platform?

    @cfg.rubygems_api_key = "x"

    File.chmod 0o644, @cfg.credentials_path

    use_ui @ui do
      assert_raise Gem::MockGemUi::TermError do
        @cfg.load_api_keys
      end
    end

    assert_empty @ui.output

    expected = <<-EXPECTED
ERROR:  Your gem push credentials file located at:

\t#{@cfg.credentials_path}

has file permissions of 0644 but 0600 is required.

To fix this error run:

\tchmod 0600 #{@cfg.credentials_path}

You should reset your credentials at:

\thttps://rubygems.org/profile/edit

if you believe they were disclosed to a third party.
    EXPECTED

    assert_equal expected, @ui.error
  end

  def test_handle_arguments
    args = %w[--backtrace --bunch --of --args here]

    @cfg.handle_arguments args

    assert_equal %w[--bunch --of --args here], @cfg.args
  end

  def test_handle_arguments_backtrace
    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: false"
    end

    util_config_file %W[--config-file=#{@temp_conf}]

    assert_equal false, @cfg.backtrace

    args = %w[--backtrace]

    @cfg.handle_arguments args

    assert_equal true, @cfg.backtrace
  end

  def test_handle_arguments_debug
    assert_equal false, $DEBUG

    args = %w[--debug]

    _, err = capture_output do
      @cfg.handle_arguments args
    end

    assert_match "NOTE", err

    assert_equal true, $DEBUG
  ensure
    $DEBUG = false
  end

  def test_handle_arguments_override
    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: false"
    end

    util_config_file %W[--backtrace --config-file=#{@temp_conf}]

    assert_equal true, @cfg.backtrace
  end

  def test_handle_arguments_traceback
    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: false"
    end

    util_config_file %W[--config-file=#{@temp_conf}]

    assert_equal false, @cfg.backtrace

    args = %w[--traceback]

    @cfg.handle_arguments args

    assert_equal true, @cfg.backtrace
  end

  def test_handle_arguments_norc
    assert_equal @temp_conf, @cfg.config_file_name

    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: false"
      fp.puts ":update_sources: false"
      fp.puts ":bulk_threshold: 10"
      fp.puts ":verbose: false"
      fp.puts ":sources:"
      fp.puts "  - http://more-gems.example.com"
    end

    args = %W[--norc]

    util_config_file args

    assert_equal true, @cfg.backtrace
    assert_equal true, @cfg.update_sources
    assert_equal Gem::ConfigFile::DEFAULT_BULK_THRESHOLD, @cfg.bulk_threshold
    assert_equal true, @cfg.verbose
    assert_equal [@gem_repo], Gem.sources
  end

  def test_load_api_keys
    temp_cred = File.join Gem.user_home, ".gem", "credentials"
    FileUtils.mkdir_p File.dirname(temp_cred)
    File.open temp_cred, "w", 0o600 do |fp|
      fp.puts ":rubygems_api_key: rubygems_b9ce70c306b3a2e248679fbbbd66722d408d3c8c4f00566c"
      fp.puts ":other: rubygems_9636a120106ea8b81fbc792188251738665711d2ece160c5"
      fp.puts "http://localhost:3000: rubygems_be293ad9dd71550a012b17d848893b41960b811ce9312b47"
    end

    util_config_file

    assert_equal(
      { :rubygems => "rubygems_b9ce70c306b3a2e248679fbbbd66722d408d3c8c4f00566c",
        :other => "rubygems_9636a120106ea8b81fbc792188251738665711d2ece160c5",
        "http://localhost:3000" => "rubygems_be293ad9dd71550a012b17d848893b41960b811ce9312b47" },
      @cfg.api_keys
    )
  end

  def test_load_api_keys_bad_permission
    pend "chmod not supported" if Gem.win_platform?

    @cfg.rubygems_api_key = "x"

    File.chmod 0o644, @cfg.credentials_path

    assert_raise Gem::MockGemUi::TermError do
      @cfg.load_api_keys
    end
  end

  def test_really_verbose
    assert_equal false, @cfg.really_verbose

    @cfg.verbose = true

    assert_equal false, @cfg.really_verbose

    @cfg.verbose = 1

    assert_equal true, @cfg.really_verbose
  end

  def test_rubygems_api_key_equals
    @cfg.rubygems_api_key = "x"

    assert_equal "x", @cfg.rubygems_api_key

    expected = {
      rubygems_api_key: "x",
    }

    assert_equal expected, load_yaml_file(@cfg.credentials_path)

    unless Gem.win_platform?
      stat = File.stat @cfg.credentials_path

      assert_equal 0o600, stat.mode & 0o600
    end
  end

  def test_rubygems_api_key_equals_bad_permission
    pend "chmod not supported" if Gem.win_platform?

    @cfg.rubygems_api_key = "x"

    File.chmod 0o644, @cfg.credentials_path

    assert_raise Gem::MockGemUi::TermError do
      @cfg.rubygems_api_key = "y"
    end

    expected = {
      rubygems_api_key: "x",
    }

    assert_equal expected, load_yaml_file(@cfg.credentials_path)

    stat = File.stat @cfg.credentials_path

    assert_equal 0o644, stat.mode & 0o644
  end

  def test_write
    @cfg.backtrace = false
    @cfg.update_sources = false
    @cfg.bulk_threshold = 10
    @cfg.verbose = false
    Gem.sources.replace %w[http://more-gems.example.com]
    @cfg[:install] = "--wrappers"

    @cfg.write

    util_config_file

    # These should not be written out to the config file.
    assert_equal true, @cfg.backtrace, "backtrace"
    assert_equal Gem::ConfigFile::DEFAULT_BULK_THRESHOLD, @cfg.bulk_threshold,
                 "bulk_threshold"
    assert_equal true, @cfg.update_sources, "update_sources"
    assert_equal true, @cfg.verbose,        "verbose"

    assert_equal "--wrappers", @cfg[:install], "install"

    # this should be written out to the config file.
    assert_equal %w[http://more-gems.example.com], Gem.sources
  end

  def test_write_from_hash
    File.open @temp_conf, "w" do |fp|
      fp.puts ":backtrace: true"
      fp.puts ":bulk_threshold: 10"
      fp.puts ":update_sources: false"
      fp.puts ":verbose: false"
      fp.puts ":sources:"
      fp.puts "  - http://more-gems.example.com"
      fp.puts ":ssl_verify_mode: 2"
      fp.puts ":ssl_ca_cert: /nonexistent/ca_cert.pem"
      fp.puts ":ssl_client_cert: /nonexistent/client_cert.pem"
      fp.puts "install: --wrappers"
    end

    util_config_file

    @cfg.backtrace = :junk
    @cfg.update_sources = :junk
    @cfg.bulk_threshold = 20
    @cfg.verbose = :junk
    Gem.sources.replace %w[http://even-more-gems.example.com]
    @cfg[:install] = "--wrappers --no-rdoc"

    @cfg.write

    util_config_file

    # These should not be written out to the config file
    assert_equal true,  @cfg.backtrace,      "backtrace"
    assert_equal 10,    @cfg.bulk_threshold, "bulk_threshold"
    assert_equal false, @cfg.update_sources, "update_sources"
    assert_equal false, @cfg.verbose,        "verbose"

    assert_equal 2,                              @cfg.ssl_verify_mode
    assert_equal "/nonexistent/ca_cert.pem",     @cfg.ssl_ca_cert
    assert_equal "/nonexistent/client_cert.pem", @cfg.ssl_client_cert

    assert_equal "--wrappers --no-rdoc", @cfg[:install], "install"

    assert_equal %w[http://even-more-gems.example.com], Gem.sources
  end

  def test_ignore_invalid_config_file
    File.open @temp_conf, "w" do |fp|
      fp.puts "invalid: yaml:"
    end

    begin
      verbose = $VERBOSE
      $VERBOSE = nil

      util_config_file
    ensure
      $VERBOSE = verbose
    end
  end

  def test_accept_string_key
    File.open @temp_conf, "w" do |fp|
      fp.puts "verbose: false"
    end

    util_config_file

    assert_equal false, @cfg.verbose
  end

  def test_load_ssl_verify_mode_from_config
    File.open @temp_conf, "w" do |fp|
      fp.puts ":ssl_verify_mode: 1"
    end
    util_config_file
    assert_equal(1, @cfg.ssl_verify_mode)
  end

  def test_load_ssl_ca_cert_from_config
    File.open @temp_conf, "w" do |fp|
      fp.puts ":ssl_ca_cert: /home/me/certs"
    end
    util_config_file
    assert_equal("/home/me/certs", @cfg.ssl_ca_cert)
  end

  def test_load_ssl_client_cert_from_config
    File.open @temp_conf, "w" do |fp|
      fp.puts ":ssl_client_cert: /home/me/mine.pem"
    end
    util_config_file
    assert_equal("/home/me/mine.pem", @cfg.ssl_client_cert)
  end

  def util_config_file(args = @cfg_args)
    @cfg = Gem::ConfigFile.new args
  end

  def test_disable_default_gem_server
    File.open @temp_conf, "w" do |fp|
      fp.puts ":disable_default_gem_server: true"
    end
    util_config_file
    assert_equal(true, @cfg.disable_default_gem_server)
  end

  def test_load_with_rubygems_config_hash
    yaml = <<~YAML
      ---
      :foo: bar
      bar: 100
      buzz: true
      alpha: :bravo
      charlie: ""
      delta:
    YAML
    actual = Gem::ConfigFile.load_with_rubygems_config_hash(yaml)

    assert_equal "bar", actual[:foo]
    assert_equal 100, actual["bar"]
    assert_equal true, actual["buzz"]
    assert_equal :bravo, actual["alpha"]
    assert_equal nil, actual["charlie"]
    assert_equal nil, actual["delta"]
  end

  def test_dump_with_rubygems_yaml
    symbol_key_hash = { foo: "bar" }

    actual = Gem::ConfigFile.dump_with_rubygems_yaml(symbol_key_hash)

    assert_equal("---\n:foo: \"bar\"\n", actual)
  end

  def test_handle_comment
    yaml = <<~YAML
      ---
      :foo: bar # buzz
      #:notkey: bar
    YAML

    actual = Gem::ConfigFile.load_with_rubygems_config_hash(yaml)
    assert_equal("bar", actual[:foo])
    assert_equal(false, actual.key?("#:notkey"))
    assert_equal(false, actual.key?(:notkey))
    assert_equal(1, actual.size)
  end

  def test_s3_source
    yaml = <<~YAML
      ---
      :sources:
      - s3://bucket1/
      - s3://bucket2/
      - s3://bucket3/path_to_gems_dir/
      - s3://bucket4/
      - https://rubygems.org/
      :s3_source:
        :bucket1:
          :provider: env
        :bucket2:
          :provider: instance_profile
          :region: us-west-2
        :bucket3:
          :id: AOUEAOEU123123AOEUAO
          :secret: aodnuhtdao/saeuhto+19283oaehu/asoeu+123h
          :region: us-east-2
        :bucket4:
          :id: AOUEAOEU123123AOEUAO
          :secret: aodnuhtdao/saeuhto+19283oaehu/asoeu+123h
          :security_token: AQoDYXdzEJr
          :region: us-west-1
    YAML

    File.open @temp_conf, "w" do |fp|
      fp.puts yaml
    end
    util_config_file

    assert_equal(["s3://bucket1/", "s3://bucket2/", "s3://bucket3/path_to_gems_dir/", "s3://bucket4/", "https://rubygems.org/"], @cfg.sources)
    expected_config = {
      bucket1: { provider: "env" },
      bucket2: { provider: "instance_profile", region: "us-west-2" },
      bucket3: { id: "AOUEAOEU123123AOEUAO", secret: "aodnuhtdao/saeuhto+19283oaehu/asoeu+123h", region: "us-east-2" },
      bucket4: { id: "AOUEAOEU123123AOEUAO", secret: "aodnuhtdao/saeuhto+19283oaehu/asoeu+123h", security_token: "AQoDYXdzEJr", region: "us-west-1" },
    }
    assert_equal(expected_config, @cfg[:s3_source])
    assert_equal(expected_config[:bucket1], @cfg[:s3_source][:bucket1])
    assert_equal(expected_config[:bucket2], @cfg[:s3_source][:bucket2])
    assert_equal(expected_config[:bucket3], @cfg[:s3_source][:bucket3])
    assert_equal(expected_config[:bucket4], @cfg[:s3_source][:bucket4])
  end

  def test_s3_source_with_config_without_lookahead
    yaml = <<~YAML
    :sources:
    - s3://bucket1/
    s3_source:
      bucket1:
        provider: env
    YAML

    File.open @temp_conf, "w" do |fp|
      fp.puts yaml
    end
    util_config_file

    assert_equal(["s3://bucket1/"], @cfg.sources)
    expected_config = {
      "bucket1" => { "provider" => "env" },
    }
    assert_equal(expected_config, @cfg[:s3_source])
    assert_equal(expected_config[:bucket1], @cfg[:s3_source][:bucket1])
  end
end
