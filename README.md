# athens-backend

## Steps to start the project locally

1. [Install Clojure](https://purelyfunctional.tv/guide/how-to-install-clojure/)
3. Clone the repo
4. run `lein run`
5. In the athens app click on Database/Library icon on the top right and navigate to Remote DB tab
6. Add your server's URL address or IP, including the default port 1337 `example.athens-backend.com:1337`
7. Add the default token : `x` (single letter).

#### Notes
* The same steps exactly can be used to test out Real Time Collaboration on your own server
* You can specify the HTTP port on which you want the backend to listen to, using the command line option `-p/--http-port` or environment variable `HTTP_PORT`

## Steps to test out **Real Time Collaboration** with Heroku

[![Deploy](https://www.herokucdn.com/deploy/button.svg)](https://heroku.com/deploy)

1. Click on the `Deploy to Heroku` button above
2. Follow the on screen instructions to make an app(give it a name) and wait until the setup is finished(~3 mins)
3. In the athens app click on Database/Library icon on the top right and navigate to Remote DB tab
4. Add remote address as `your-app-name.herokuapp.com` and token as `x` (single letter). For eg. if your app name on heroku is `test-app` then the remote address is `test-app.herokuapp.com`
5. Share address and token with your team and start simultaneously working on the same graph!

#### Important Note

This is only a **test deployment** and **all changes will be lost** once heroku dyno reloads(which happens once a day atleast)

