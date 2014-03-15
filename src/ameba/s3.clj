(ns ameba.s3
  "Functions related to storing blobs and clobs in AWS S3"
  (:require [aws.sdk.s3 :as s3]
            [clojure.tools.logging :as log]
            [ameba.core :refer [md5 cache]]
            [clojure.java.io :refer [as-url]])
  (:import [java.net URI]))




(defn bucket-url-str
  "url for the given s3 bucket"
  [bucket-name]
  (str "http://" bucket-name ".s3.amazonaws.com/"))

(defn object-uri
  "Given the ID of an s3 object, yields the url to access that object.
   If obj-id is nil, returns the empty string"
  [bucket obj-id]
  (if obj-id
    (URI. (str (bucket-url-str bucket) obj-id))
    ""))

(defn put-text
  "Uploads the given text to S3 storage, and returns the key. The key is generated
   via md5 checksum."
  [s3-cred s3-bucket metadata permissions text]
  (let [key (md5 text)]
    (when-not (s3/object-exists? s3-cred s3-bucket key)
      (s3/put-object s3-cred s3-bucket key text metadata permissions))
    key))

(defn s3-text-putter
  "Produces a function which uploads text to a specified S3 bucket. The function returns
   the key, which is generated via md5"
  [s3-cred s3-bucket & [metadata & permissions]]
  (partial put-text s3-cred s3-bucket metadata permissions))

(defn get-text
  ""
  [s3-cred s3-bucket key]
    (log/info (str "GETTING FROM S3 " (class key) " " key " " s3-cred " " s3-bucket))
  (if-let [obj (s3/get-object s3-cred s3-bucket key)]
    (slurp (:content obj))
    nil))

(defn s3-text-getter
  "Produces a function which gets an S3 object's context from a specific S3 bucket"
  [s3-cred s3-bucket]
  (partial get-text s3-cred s3-bucket))
