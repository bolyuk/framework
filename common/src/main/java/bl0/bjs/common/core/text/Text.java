package bl0.bjs.common.core.text;

public class Text {
    private String content;

    public Text(String content) {
        this.content = content;
    }

    public Text replace(String field, Text value){
        return replace(field, value.content);
    }

    public Text replace(String field, String value){
        this.content = this.content.replace(prepareField(field), value);
        return this;
    }

    public Text replaceAll(String field, Text value){
        return replaceAll(field, value.content);
    }

    public Text replaceAll(String field, String value){
        this.content = this.content.replaceAll(prepareField(field), value);
        return this;
    }

    public String get(){
        return this.content;
    }

    @Override
    public String toString() {
        return content;
    }

    private String prepareField(String field){
        return "{"+field+"}";
    }

    public static Text build(String content){
        return new Text(content);
    }
}
