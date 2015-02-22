package jest.compiler;


import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import java.util.List;

public abstract class Code {

    public static class CodeException extends RuntimeException{}

    public abstract Boolean isSingleLine();

    public abstract List<String> getMultiLine();

    public abstract String getSingleLine();


    public static Code singleLine(String code) {
        return new SingleLine(code);
    }

    public static Code multiLine(Iterable<String> code) {
        return new MultiLine(code);
    }

    public static Code empty() {
        return Code.multiLine(ImmutableList.<String>of());
    }

    /*
    public static String getCodeList(Code code) {
        if (code.isSingleLine()) {
            return code.getSingleLine();
        } else {
            return Joiner.on("\n").join(code.getMultiLine());
        }
    }
*/

    public List<String> getLines() {
        if (this.isSingleLine()) {
            return ImmutableList.of(this.getSingleLine());
        } else {
            return ImmutableList.copyOf(this.getMultiLine());
        }
    }


    public static class SingleLine extends Code {

        public SingleLine(String code) {
            this.code = code;
        }

        protected final String code;

        @Override
        public Boolean isSingleLine() {
            return true;
        }

        @Override
        public List<String> getMultiLine() {
            throw new CodeException();
        }

        @Override
        public String getSingleLine() {
            return this.code;
        }


    }


    public static class MultiLine extends Code {

        public MultiLine(Iterable<String> code) {
            this.code = ImmutableList.copyOf(code);
        }

        protected final ImmutableList<String> code;

        @Override
        public Boolean isSingleLine() {
            return false;
        }

        @Override
        public List<String> getMultiLine() {
            return code;
        }

        @Override
        public String getSingleLine() {
            throw new CodeException();
        }
    }

}
