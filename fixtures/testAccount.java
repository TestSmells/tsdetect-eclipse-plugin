import static org.junit.Assert.*;

import org.junit.Test;

/**
 * Fixture for the Dependent Test detector: testBalanceAfterDeposit calls
 * testDeposit, so the two only pass in a particular order.
 */
public class testAccount {

    @Test
    public void testDeposit() {
        Account account = new Account();
        account.deposit(50);
        assertEquals(50, account.getBalance());
    }

    @Test
    public void testBalanceAfterDeposit() {
        testDeposit();
        Account account = new Account();
        assertEquals(0, account.getBalance());
    }
}
