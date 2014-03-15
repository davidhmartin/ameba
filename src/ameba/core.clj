(ns ameba.core
  (:import [org.apache.commons.codec.binary Base32 Base64]
           [java.nio ByteBuffer]))

;; A unique-enough ID generator producing a long. Appends a 16-bit sequence number to a 48-bit
;; timestamp.
(def uid-counter (ref 0))
(defn- next-counter [] (dosync (alter uid-counter #(bit-and (inc %) 0xffff))))
(defn next-uid
  "A unique-enough ID generator producing a long. Appends a 16-bit sequence number to a 48-bit timestamp."
  []
  (bit-or (bit-shift-left (System/currentTimeMillis) 16) (next-counter))) 

;; (defn next-uid-b32 []
;;   (.encodeToString (Base32.) (.. ByteBuffer (allocate 8) (putLong (next-uid)) (array))))

(defn- trim-padding
  [encoded]
  (.. encoded (substring 0 (.indexOf encoded "="))))
(defn next-uid-b32
  "Generates unique ID in base32-encoded format"
  []
  (trim-padding (.. (Base32.) (encodeToString (.. ByteBuffer (allocate 8) (putLong (next-uid)) (array))))))

(defn next-uid-b64
  "Generates unique ID in base64-encoded format"
  []
  (trim-padding  (.. (Base64.) (encodeToString (.. ByteBuffer (allocate 8) (putLong (next-uid)) (array))))))




(defn md5
  "Generate a md5 checksum for the given string"
  [token]
  (let [hash-bytes
         (doto (java.security.MessageDigest/getInstance "MD5")
               (.reset)
               (.update (.getBytes token)))]
       (.toString
         (new java.math.BigInteger 1 (.digest hash-bytes)) ; Positive and the size of the number
         16))) ; Use base16 i.e. hex
