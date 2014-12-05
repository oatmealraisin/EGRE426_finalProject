public class Register {

    int value;
    boolean used;

    public Register(int _value) {
	value = _value;
	used = false;
    }

    public void changeStatus() {
	used = !used;
    }

    public void setStatus(boolean _used) {
	used = _used;
    }

    public boolean getStatus() {
	return used;
    }

    public int getValue() {
	return value;
    }

    public void setValue(int _value) {
	value = _value;
    }
}
