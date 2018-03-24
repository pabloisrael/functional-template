
(ns exercise3)

(def fib-seq
     (lazy-cat [0 1] (map + (rest fib-seq) fib-seq)))

(defn fibonacci
	[x]
	(nth fib-seq x))