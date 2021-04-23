prod-build: clean
	lein uberjar

dev-run: clean
	lein run

clean:
	lein clean

fetch-deps:
	lein deps

