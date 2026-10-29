
package Blood.Donation.System;

public class BloodTypes {
    private String id;
    private String processor;
    private String ram;
    private String storage;
    private String os;
    private String status;

    public BloodTypes(String id, String processor, String ram, String storage, String os, String status) {
        this.id = id;
        this.processor = processor;
        this.ram = ram;
        this.storage = storage;
        this.os = os;
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public String getProcessor() {
        return processor;
    }

    public String getRam() {
        return ram;
    }

    public String getStorage() {
        return storage;
    }

    public String getOs() {
        return os;
    }

    public String getStatus() {
        return status;
    }

    public void setProcessor(String processor) {
        this.processor = processor;
    }

    public void setRam(String ram) {
        this.ram = ram;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
