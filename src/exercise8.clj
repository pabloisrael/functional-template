
(ns exercise8)

(defn fderive [func deltaH]
    (fn [x]
      (/ (- (func (+ x deltaH)) (func (- x deltaH))) (* 2 deltaH))
      )
    )