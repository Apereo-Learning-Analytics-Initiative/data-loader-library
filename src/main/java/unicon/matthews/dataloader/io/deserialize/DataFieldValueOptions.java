package unicon.matthews.dataloader.io.deserialize;

public enum DataFieldValueOptions {

    NULL("\\N");

    private final String fieldValue;

    DataFieldValueOptions(String fieldValue) {
        this.fieldValue = fieldValue;
    }

    public String getFieldValue() {
        return this.fieldValue;
    }

}
