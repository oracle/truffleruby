# Rails Demo - Blog

-   Configure `config/database.yml` (Postgresql required)
-   Start Postgres (eg `postgres -D /usr/local/var/postgres`)
-   Setup environment: `truffleruby-tool setup --offline`
-   Create database: `truffleruby-tool run bin/rake db:create`   
-   Migrate database: `truffleruby-tool run bin/rake db:migrate`   
-   Run Rails server: `truffleruby-tool run bin/rails server`
-   Go to <http://localhost:3000>
-   Create a new post with:

        = This is the Title of the blog post
        Author Name
        :icons: font
         
        This is an *example* of a _blog post_.
        
        == Header 1
        
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus est ante, 
        congue aliquet suscipit vel, mollis ac quam. Nam aliquam porta massa, non 
        porttitor risus cursus quis. Quisque suscipit, lorem eget congue semper, 
        sem tortor volutpat arcu, non volutpat libero felis et eros. 
        
        * Item 1
        * Item 2
        * Item 3
        
        Lorem ipsum dolor sit amet, consectetur adipiscing elit. Phasellus est ante, 
        congue aliquet suscipit vel, mollis ac quam.        
        
        NOTE: This is really just an example.
         
## Alternatively using `bundler`

-   _Omitting first 2 steps from above._     
-   Install stubs `truffleruby-tool setup --no-bundler` (used by bundle exec)
-   Setup environment variables
    -   `export JRUBY_OTPS='-X+T'` (fish: `set -x JRUBY_OPTS '-X+T'`)
    -   `export RUBYOTP='-r ./workarounds'` (fish: `set -x RUBYOPT '-r ./workarounds'`)
-   Install gems `bundle install`
-   Create database: `bundle exec bin/rake db:create`    
-   Migrate database: `bundle exec bin/rake db:migrate`   
-   Run Rails server: `bundle exec bin/rails server`
-   _Remaining steps omitted._
