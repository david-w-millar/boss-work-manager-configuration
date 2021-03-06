(ns boss-work-manager-configuration.ingest
  (:require [clojure.string :as s]))

(defn write-lines
  "Coerces path to a java.io.Writer and attempts to write a sequence of
  newline-separate strings to it, as specified by lines."
  [path lines]
  (with-open [f (clojure.java.io/writer path)]
    (doseq [l lines]
      (.write f l)
      (.newLine f))))

(def camel-seq
  "Returns a lazy sequence of strings that represent individual word components
  in a camel-cased string."
  (partial re-seq #"(?:^[a-z]|[A-Z])[^A-Z]+|[A-Z]+$|[A-Z]{2,}(?=[A-Z])"))

(comment (defn tokenize-camel-symbols
  "Returns a lazy sequence of camel-case word tokens by decomposing each string in
  coll, surrounding it in with ':start' and ':end' delimiters, and concatenating
  everything together. e.g.

  (tokenize-camel-symbols [\"FooBar\" \"aThingToDo\" \"MyClass\"])
  => (\":start\" \"Foo\" \"Bar\" \":end\"
      \":start\" \"a\" \"Thing\" \"To\" \"Do\" \":end\"
      \":start\" \"My\" \"Class\" \":end\")"
  [coll]
  (mapcat (comp #(concat [":start"] % [":end"])
                camel-seq)
          coll)))

(defn wrap-delimiters
  "Adds the strings \":start\" and \":end\" to the start and end of a sequence
  of tokens, respectively."
  [tokens]
  (concat [":start"] tokens [":end"]))

(defn split-whitespace
  "Splits a string on whitespace."
  [s]
  (s/split s #"\s+"))

(defn write-tokens
  "Writes a sequence of tokens to path by using f to transform each element of
  coll into a sequence of tokens and wrapping each of these sub-sequences with
  delimiters (see wrap-delimiters)."
  [path f coll]
  (->> coll
       (mapcat (comp wrap-delimiters f))
       (write-lines path)))
