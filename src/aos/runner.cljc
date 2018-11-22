(ns aos.runner
  (:require
   [clojure.test :as t]

   ;; [speculative.instrument]
   [clojure.string :as str]
   [speculative.core] ;; something is the matter with merge-with spec
   [clojure.spec.alpha :as s]
   [speculative.specs :as ss]
   [speculative.set]
   [speculative.string]
   [speculative.test :refer [deftime ?]]

   [aos.y2017.d01]
   [aos.y2017.d02]))

(clojure.spec.test.alpha/instrument)

#_(defn planck-env? []
  #?(:cljs (exists? js/PLANCK_EXIT_WITH_VALUE)
     :clj false))

#_(defn exit
  "Exit with the given status."
  [status]
  #?(:cljs
     (when-let
         [exit-fn
          (cond
            ;; node
            (exists? js/process)
            #(.exit js/process %)
            ;; nashorn
            (exists? js/exit)
            js/exit
            ;; planck
            (planck-env?)
            js/PLANCK_EXIT_WITH_VALUE)]
       (exit-fn status))
     :clj (do
            (shutdown-agents)
            (System/exit status))))

#?(:cljs
   (do
     (defmethod cljs.test/report [:cljs.test/default :begin-test-var] [m]
       ;; for debugging:
       ;; (println ":begin-test-var" (cljs.test/testing-vars-str m))
       )
     (defmethod cljs.test/report [:cljs.test/default :end-run-tests] [m]
       (if-not (cljs.test/successful? m)
         (exit 1)
         (exit 0))))
   :clj
   nil #_(defmethod clojure.test/report :summary [m]
     (clojure.test/with-test-out
       (println "\nRan" (:test m) "tests containing"
                (+ (:pass m) (:fail m) (:error m)) "assertions.")
       (println (:fail m) "failures," (:error m) "errors."))
     (when-not (clojure.test/successful? m)
       (exit 1)
       #_(exit 0))))

(defn -main [& args]
  (println "enabled?" (deref #'clojure.spec.test.alpha/*instrument-enabled*))
  (t/run-tests 'aos.y2017.d01
               'aos.y2017.d02)
  (println "enabled?" (deref #'clojure.spec.test.alpha/*instrument-enabled*)))

#?(:cljs (set! *main-cli-fn* -main))
