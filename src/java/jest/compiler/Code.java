package jest.compiler;


import com.google.common.collect.ImmutableList;
import java.util.List;

public interface Code {

    public static class CodeException extends RuntimeException{}

    Boolean isSingleLine();

    List<String> getMultiLine();

    String getSingleLine();


    public static class SingleLine implements Code {

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


    public static class MultiLine implements Code {

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
