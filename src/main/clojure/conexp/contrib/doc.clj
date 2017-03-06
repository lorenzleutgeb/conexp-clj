;; Copyright ⓒ The conexp-clj developers; all rights reserved.
;; The use and distribution terms for this software are covered by the
;; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
;; which can be found in the file LICENSE at the root of this distribution.
;; By using this software in any fashion, you are agreeing to be bound by
;; the terms of this license.
;; You must not remove this notice, or any other, from this software.

(ns conexp.contrib.doc
  (:use conexp.main)
  (:use [clojure.string :only (split replace)
                        :rename {replace replace-str}]
        [clojure.pprint :only (cl-format)]
        [clojure.java.io :only (writer)]))


;;; API Documentation

(defn public-api
  "Returns a map of public functions of namespaces to pairs of their argumentation list
  and their documentation."
  [ns]
  (let [ns (find-ns ns)]
    (into {}
          (map (fn [[function var]]
                 [function [(:arglists (meta var)) (:doc (meta var))]])
               (filter (fn [[f var]]
                         (and (= (:ns (meta var)) ns)
                              (not (Character/isUpperCase ^Character (first (str f))))))
                       (ns-map ns))))))

;;; Documentation Coverage

(defn conexp-fns-needing-doc
  "Returns function in public conexp-clj API not having documentation."
  []
  (for [ns conexp-clj-namespaces,
        [f _] (public-api ns)
        :when (not (:doc (meta (resolve (symbol (str ns) (str f))))))]
    (symbol (str ns) (str f))))

;;; API Documentation

(defn public-api-to-markdown
  "Prints to standard out the API documentation of the main namespaces of conexp-clj, or
  the namespaces provided as argument."
  ([]
     (public-api-to-markdown conexp-clj-namespaces))
  ([namespaces]
     (doseq [ns namespaces]
       (require ns))
     (cl-format true "~%# API Documentation")
     (doseq [ns namespaces]
       (cl-format true "~%~% - Namespace [`~A`](#~A)" ns ns))
     (doseq [ns namespaces]
       (cl-format true "~%~%## <a name=\"~A\"> Public API of ~A ~%" ns ns)
       (when-let [doc (:doc (meta (find-ns ns)))]
         (cl-format true "~%~%  ~A" doc))
       (cl-format true "~%~%### Available Functions ~%")
       (doseq [[f _] (sort-by first (public-api ns))]
         (cl-format true "~%- [`~A`](#~A)" f f))
       (cl-format true "~%~%### Function Documentation ~%")
       (doseq [[f [arglist doc]] (sort-by first (public-api ns))]
         (when doc
           (let [;; No > in link-names
                 link-f  (apply str (replace {\> "&gt;"} (str f)))
                 ;; git's markdown has problems with [[ in code blocks
                 arglist (apply str (replace {\[ "[&zwj;"} (str arglist)))
                 ;; block-quote docstrings
                 doc     (reduce (fn [a b]
                                   (str a "\n>   " b))
                                 ""
                                 (map #(nth (re-find #"(  )?(.*)$" %1) 2)
                                      (split doc #"\n")))]
             (cl-format true
                        "~%#### <a name=\"~A\"> Function `~A` ~%~%Argument List: <tt>~A</tt> ~%~%Documentation: ~A ~%"
                        link-f f arglist doc)))))))

(defn public-api-to-file
  "Writes the API documentation as generated by public-api-to-markdown to the given file,
  i.e. redirects standard output to file and applies public-api-to-markdown on args."
  [file & args]
  (binding [*out* (writer file)]
    (apply public-api-to-markdown args)))

(defn make-conexp-standard-api-documentation
  "Generates the standard conexp-clj API documentation and writes it to the specified
  file"
  [file]
  (public-api-to-file
   file '[conexp.main
          conexp.layouts
          conexp.layouts.base
          conexp.layouts.common
          conexp.layouts.force
          conexp.layouts.freese
          conexp.layouts.layered
          conexp.layouts.util
          conexp.contrib.concept-approximation
          conexp.contrib.doc
          conexp.contrib.fuzzy.fca
          conexp.contrib.fuzzy.logics
          conexp.contrib.fuzzy.sets
          conexp.contrib.draw
          conexp.contrib.exec
          conexp.contrib.factor-analysis
          conexp.contrib.gui
          conexp.contrib.java
          conexp.contrib.nonsense
          conexp.contrib.profiler
          conexp.contrib.retracts
          ]))

;;;

nil
