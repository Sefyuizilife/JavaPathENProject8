package tourguide.helper;

public final class InternalTestHelper {

    // Set this default up to 100,000 for testing
    private static int INTERNAL_USER_NUMBER = 100;

    public static int getInternalUserNumber() {

        return INTERNAL_USER_NUMBER;
    }

    public static void setInternalUserNumber(int internalUserNumber) {

        InternalTestHelper.INTERNAL_USER_NUMBER = internalUserNumber;
    }
}
