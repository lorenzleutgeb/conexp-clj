all: zip

zip: jar
	mkdir -p conexp-clj/lib/
	cp stuff/libs/*.jar conexp-clj/lib/
	cp stuff/libs/*.clj conexp-clj/lib
	cp -r stuff/bin res AUTHORS LICENSE README.md conexp-clj/
	cp lib/*.jar conexp-clj/lib/
	mv conexp-clj-*.jar conexp-clj/lib/
	zip -r conexp-clj-$(cat VERSION).zip conexp-clj

jar: distclean
	lein jar

clean:
	rm -rf conexp-clj/

distclean: clean
	lein clean
	rm -f conexp-clj-$(cat VERSION).zip

test:
	lein test!

test-zip:
