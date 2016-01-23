package jest.compiler;


import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import jest.Utils.Named;
import jest.compiler.Types.DeclaredFunctionDeclaration;
import jest.compiler.Types.FunctionDeclaration;
import jest.compiler.Types.GenericType;
import jest.compiler.Types.Type;

import static jest.Utils.asType;


public class Core {


    public enum PrimitiveType implements Type, Named  {

        Any {
            @Override
            public String getName() {
                return "Any";
            }
        },
        String {
            @Override
            public String getName() {
                return "String";
            }
        },
        Symbol {
            @Override
            public String getName() {
                return "Symbol";
            }
        },
        Number {
            @Override
            public String getName() {
                return "Number";
            }
        },
        Boolean {
            @Override
            public String getName() {
                return "Boolean";
            }
        },
        Nil {
            @Override
            public String getName() {
                return "Nil";
            }
        };

        @Override
        public Boolean implementsType(Type other) {
            return this.equals(other);
        }

        @Override
        public List<Type> getDependentTypes() {
            return ImmutableList.of();
        }

        @Override
        public Type getBaseType() {
            return this;
        }

        @Override
        public Boolean isGeneric() {
            return false;
        }

    }


    public enum CollectionType implements Type, Named {

        Map {
            @Override
            public String getName() {
                return "Map";
            }
        },
        List {
            @Override
            public String getName() {
                return "List";
            }
        },
        Vector {
            @Override
            public String getName() {
                return "Vector";
            }
        };

        @Override
        public Boolean implementsType(Type other) {
            // TODO: Fix this
            return this.equals(other);
        }

        @Override
        public List<Type> getDependentTypes() {
            return ImmutableList.of();
        }

        @Override
        public Type getBaseType() {
            return this;
        }

        @Override
        public Boolean isGeneric() {
            return false;
        }
    }

    public static final Map<String, FunctionDeclaration> coreFunctions = ImmutableMap.<String, FunctionDeclaration>builder()

        .put("range", new DeclaredFunctionDeclaration("range",
            ImmutableList.of("start", "stop", "step"),
            ImmutableList.<Type>of(PrimitiveType.Number, PrimitiveType.Number, PrimitiveType.Number),
            new GenericType("List", PrimitiveType.Number)))

        .build();


    public static final Set<String> clojureCoreFunctionsNoSig = ImmutableSet.of("accessor","aclone","add-classpath","add-watch","agent","agent-error",
        "agent-errors","aget","alength","alias","all-ns","alter","alter-meta!","alter-var-root","amap","ancestors","and","apply","areduce",
        "array-map","as->","aset","aset-boolean","aset-byte","aset-char","aset-double","aset-float","aset-int","aset-long","aset-short","assert",
        "assoc","assoc!","assoc-in","associative?","atom","await","await-for","bases","bean","bigdec","bigint","biginteger","binding","bit-and",
        "bit-and-not","bit-clear","bit-flip","bit-not","bit-or","bit-set","bit-shift-left","bit-shift-right","bit-test","bit-xor","boolean",
        "boolean-array","booleans","bound-fn","bound-fn*","bound?","butlast","byte","byte-array","bytes","case","cast","catch","char",
        "char-array","char-escape-string","char-name-string","char?","chars","class","class?","clear-agent-errors","clojure-version",
        "coll?","comment","commute","comp","comparator","compare","compare-and-set!","compile","complement","concat","cond","cond->",
        "cond->>","condp","conj","conj!","cons","constantly","construct-proxy","contains?","count","counted?","create-ns","create-struct",
        "cycle","dec","dec\'","decimal?","declare","def","default-data-readers","definline","definterface","defmacro","defmethod","defmulti",
        "defn","defn-","defonce","defprotocol","defrecord","defstruct","deftype","delay","delay?","deliver","denominator","deref","derive",
        "descendants","disj","disj!","dissoc","dissoc!","distinct","distinct?","do","doall","dorun","doseq","dosync","dotimes","doto","double",
        "double-array","doubles","drop","drop-last","drop-while","empty","empty?","ensure","enumeration-seq","error-handler","error-mode","eval",
        "even?","every-pred","every?","ex-data","ex-info","extend","extend-protocol","extend-type","extenders","extends?","false?","ffirst",
        "file-seq","filter","filterv","finally","find","find-keyword","find-ns","find-var","first","flatten","float","float-array","float?",
        "floats","flush","fn","fn?","fnext","fnil","for","force","format","frequencies","future","future-call","future-cancel","future-cancelled?",
        "future-done?","future?","gen-class","gen-interface","gensym","get","get-in","get-method","get-proxy-class","get-thread-bindings",
        "get-validator","group-by","hash","hash-map","hash-ordered-coll","hash-set","hash-unordered-coll","identical?","identity","if","if-let",
        "if-not","if-some","ifn?","import","in-ns","inc","inc\'","init-proxy","instance?","int","int-array","integer?","interleave","intern",
        "interpose","into","into-array","ints","io!","isa?","iterate","iterator-seq","juxt","keep","keep-indexed","key","keys","keyword","keyword?",
        "last","lazy-cat","lazy-seq","let","letfn","line-seq","list","list*","list?","load","load-file","load-reader","load-string","loaded-libs",
        "locking","long","long-array","longs","loop","macroexpand","macroexpand-1","make-array","make-hierarchy","map","map-indexed","map?",
        "mapcat","mapv","max","max-key","memfn","memoize","merge","merge-with","meta","methods","min","min-key","mix-collection-hash","mod",
        "monitor-enter","monitor-exit","name","namespace","namespace-munge","neg?","new","newline","next","nfirst","nil?","nnext","not",
        "not-any?","not-empty","not-every?","not=","ns","ns-aliases","ns-imports","ns-interns","ns-map","ns-name","ns-publics","ns-refers",
        "ns-resolve","ns-unalias","ns-unmap","nth","nthnext","nthrest","num","number?","numerator","object-array","odd?","or","parents",
        "partial","partition","partition-all","partition-by","pcalls","peek","persistent!","pmap","pop","pop!","pop-thread-bindings","pos?",
        "pr","pr-str","prefer-method","prefers","print","print-str","printf","println","println-str","prn","prn-str","promise","proxy",
        "proxy-mappings","proxy-super","push-thread-bindings","pvalues","quot","quote","rand","rand-int","rand-nth","range","ratio?",
        "rational?","rationalize","re-find","re-groups","re-matcher","re-matches","re-pattern","re-seq","read","read-line","read-string",
        "realized?","record?","recur","reduce","reduce-kv","reduced","reduced?","reductions","ref","ref-history-count","ref-max-history",
        "ref-min-history","ref-set","refer","refer-clojure","reify","release-pending-sends","rem","remove","remove-all-methods","remove-method",
        "remove-ns","remove-watch","repeat","repeatedly","replace","replicate","require","reset!","reset-meta!","resolve","rest","restart-agent",
        "resultset-seq","reverse","reversible?","rseq","rsubseq","satisfies?","second","select-keys","send","send-off","send-via","seq","seq?",
        "seque","sequence","sequential?","set","set!","set-agent-send-executor!","set-agent-send-off-executor!","set-error-handler!",
        "set-error-mode!","set-validator!","set?","short","short-array","shorts","shuffle","shutdown-agents","slurp","some","some->",
        "some->>","some-fn","some?","sort","sort-by","sorted-map","sorted-map-by","sorted-set","sorted-set-by","sorted?","special-symbol?",
        "spit","split-at","split-with","str","string?","struct","struct-map","subs","subseq","subvec","supers","swap!","symbol","symbol?","sync",
        "take","take-last","take-nth","take-while","test","the-ns","thread-bound?","throw","time","to-array","to-array-2d","trampoline","transient",
        "tree-seq","true?","try","type","unchecked-add","unchecked-add-int","unchecked-byte","unchecked-char","unchecked-dec","unchecked-dec-int",
        "unchecked-divide-int","unchecked-double","unchecked-float","unchecked-inc","unchecked-inc-int","unchecked-int","unchecked-long",
        "unchecked-multiply","unchecked-multiply-int","unchecked-negate","unchecked-negate-int","unchecked-remainder-int","unchecked-short",
        "unchecked-subtract","unchecked-subtract-int","underive","unsigned-bit-shift-right","update-in","update-proxy","use","val","vals","var",
        "var-get","var-set","var?","vary-meta","vec","vector","vector-of","vector?","when","when-first","when-let","when-not","when-some",
        "while","with-bindings","with-bindings*","with-in-str","with-local-vars","with-meta","with-open","with-out-str","with-precision",
        "with-redefs","with-redefs-fn","xml-seq","zero?","zipmap");


    public static final Set<String> clojreCoreReducers = ImmutableSet.of("append!","cat","drop","filter","flatten","fold","foldcat","folder",
        "map","mapcat","monoid","reduce","reducer","remove","take","take-while");


    public static final Set<String> clojureCore = ImmutableSet.<String>builder()
        .addAll(clojureCoreFunctionsNoSig)
        .addAll(clojreCoreReducers)
        .build();

}
