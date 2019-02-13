Como hacer set up de un nuevo server con entorno de producción:
 
### Creación del subdominio
 
### Creación de la carpeta de la aplicación en el server

Conectarse al server a traves de __ssh__. (previamente haber configurado correctamente el archivo ~/.ssh/config con el Host, Hostname, User, IdentityFile(opcional) )
 
Loguearse con el usuario deploy: `$ sudo su deploy`.
 
Crear la carpeta de la app en el directorio __/srv/rails__: `$ mkdir /srv/rails/portfolio-api`. Es muy importante ponerle el mismo nombre con el que se dio de alta el subdominio.
 
### Deploy
Ir al proyecto en gitLab y con rol de admin entrar en la sección de "Deploy Keys" dentro de configuración del proyecto.
 
Habilitar la key de deploy del server para ese proyecto. Si no esta, agregarla, obteniéndola del server con `$ cat ~/.ssh/id_rsa.pub`.
 
Ir al código del proyecto, crear el archivo __config/deploy/production.rb__ si no está creado.
Hacer set up del archivo __production.rb__ copiándolo de development y cambiándole la branch y el server. Por ejemplo: 

```ruby
set :rails_env, 'production'
set :branch, 'production'
set :deploy_to, '/srv/rails/portfolio-api'
 
server 'portfolio-api.amalgama.co',
user: 'deploy',
roles: %w{app db web}
```
 
Chequear que exista la branch production en el repositorio remoto.
Chequear que exista el archivo el archivo __config/deploy.rb__ sino crearlo. Por ejemplo:

```ruby
set :application,     'portfolio'
set :repo_url,        'git@git.theamalgama.com:portfolio/portfolio-ws.git'
set :deploy_to, '/srv/rails/portfolio'
set :keep_releases, 2
set :linked_dirs, fetch( :linked_dirs, [] ).push( 'log', 'tmp/pids', 'tmp/cache', 'tmp/sockets', 'public/system', 'public/uploads' )
set :linked_files, fetch( :linked_files, [] ).push( 'config/database.yml', 'config/secrets.yml')
```

Chequear que exista el archivo __Capfile__ sino crearlo. Por ejemplo:

```ruby
# Load DSL and Setup Up Stages
require 'capistrano/setup'
require 'capistrano/deploy'
require 'capistrano/rvm'
require 'capistrano/bundler'
require 'capistrano/rails'
require 'capistrano/rails/assets'
require 'capistrano/rails/migrations'
require 'capistrano/puma'
require "capistrano/scm/git"
install_plugin Capistrano::SCM::Git

# Loads custom tasks from `lib/capistrano/tasks' if you have any defined.
Dir.glob('lib/capistrano/tasks/*.rake').each { |r| import r }
```
 
__Checkpoint__: hacer deploy para probar con `$ bundle exec cap production deploy`.
Conectarse al server ir a la carpeta __config/shared__: `$ cd /srv/rails/portfolio-api/shared/config`.

Crear el archivo __database.yml__: `$ touch database.yml` con los datos que vienen por defecto en el database.example.yml, luego modifcarlo con la configuración requerida. 

Ejemplo:
```ruby
default: &DEFAULT
  adapter: mysql2
  pool: 5
  timeout: 5000
  host: 127.0.0.1
  encoding: utf8
  username: {usuario en mysql}
  password: {password del usuario de mysql}
 
production:
  <<: *DEFAULT
  database: portfolio_production
```

Crear el archivo secrets.yml: `$ touch secrets.yml`.

Ejemplo: 
Crear el archivo secrets.yml dentro del directorio shared/config . Completarlo con:

```ruby
production:
 secret_key_base: {key}
```
 
__NOTA__: La key se puede sacar de development por ahora.

Chequear que la version de ruby en el server sea la correcta.
 
### Crear Base de Datos

- MySQL: Conectarse al servidor de __mysql__ que va a usar la aplicación con usuario __root__ y crear las bases de datos de production. Ejemplo del comando `> CREATE DATABASE portfolio_production;`. Crear un usuario de mysql para la aplicación y darle acceso a las bases de datos con el comando `> GRANT ALL PRIVILEGES ON portfolio_production.* To 'portfolio'@'localhost' IDENTIFIED BY '{password}'`;

- PostgreSQL: Loguearse con el usuario postgres `$ sudo su postgres`. Con el comando `$ psql` nos conectamos con el servidor de postgres. Creamos la base de datos `CREATE DATABASE portfolio_prod;`. Creamos el usuario con su password correspondiente `CREATE USER portfolio_prod WITH PASSWORD 'portfolio_prod';`
 
Volver a loguearse con el usuario deploy.

Ir al directorio `$ cd /etc/nginx/sites-available`.
Crear el archivo para la app con el mismo nombre por ejemplo:
`$ sudo vim portfolio-api`

```nginx
upstream portfolio-api {
    server unix:///srv/rails/portfolio-api/shared/tmp/sockets/puma.sock fail_timeout=10;
}
 
server {
    	listen 80;
        server_name portfolio-api.theamalgama.com portfolio-api.amalgama.co;
 
 
    	root /srv/rails/portfolio-api/current/public;
 
    	try_files $uri/index.html $uri @portfolio-api;
 
    	access_log /var/log/nginx/access/portfolio-api.access.log;
    	error_log /var/log/nginx/errors/portfolio-api.error.log;
 
        location / {
                try_files $uri @portfolio-api;
        }
 
        location @portfolio-api {
        	proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        	proxy_set_header Host $http_host;
        	proxy_redirect off;
        	proxy_pass http://portfolio-api;
    	}
 
    	error_page 500 502 503 504 /500.html;
    	client_max_body_size 4G;
    	keepalive_timeout 10;
}
```

Chequear que el formato del archivo de configuración es el correcto con el comando `$ sudo nginx -t`. Si esta todo bien la salida debe ser: 
```
nginx: the configuration file /etc/nginx/nginx.conf syntax is ok
nginx: configuration file /etc/nginx/nginx.conf test is successful
```

### Crear un symbolic link en sitios habilitados

Ir al directorio `$ cd /etc/nginx/sites-enabled`.
Crear el symbolic link con el comando `$ sudo ln -s /etc/nginx/sites-available/portfolio-api portfolio-api`

Reiniciar nginx con el comando `$ sudo service nginx restart
