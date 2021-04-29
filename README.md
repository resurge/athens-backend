# athens-backend

## Development

To start the project locally: [install Clojure](https://purelyfunctional.tv/guide/how-to-install-clojure/), clone the repo, and run `lein run`.

You might want to specify HTTP port that backend will listen to.
Default value is: 1337
Provide your own port number via command line option `-p/--http-port`
or environment variable `HTTP_PORT`.

## Deployment

### Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)


Steps to test out **Real time collaboration**
1. Click on the `Deploy to Heroku`
2. Follow the on screen instructions to make an app(give it a name) and wait until the setup is finished(~3 mins)
3. In the athens app click on Database/Library icon on the top right and navigate to Remote DB tab
4. Add remote address as `your-app-name.herokuapp.com` and token as `x` (single letter). For eg. if your app name on heroku is `test-app` then the remote address is `test-app.herokuapp.com`
5. Share address and token with your team and start simultaneously working on the same graph!

#### Important Note

This is only a **test deployment** and **all changes will be lost** once heroku dyno reloads(which happens once a day atleast)

### Docker

1. Clone the repo
2. `docker build -t athens-backend .`
3. `docker run -it -p 13337:13337 athens-backend`

Note that the Docker container exposes "13337" by default instead of "1337" so that this container can run in less privileged environments which may not allow low-ports. It can still be overridden by the HTTP_PORT variable.
