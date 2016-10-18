package chat;

/**
 * Created by weijiangan on 03/10/2016.
 */
public class Authenticator {
    User[] users = {};

    /* To be implemented later
    Authenticator(String fileName) {

    } */

    Authenticator() {
        users = new User[] {
                new User("Distributed", "123456", User.Type.CLIENT),
                new User("Client1", "123456", User.Type.CLIENT),
                new User("Client3", "123456", User.Type.CLIENT),
                new User("Agent007", "jamesb", User.Type.AGENT),
                new User("Agent008", "lousy", User.Type.AGENT)
        };
    }

    public int authenticate(User login) {
        int signal = 0;

        for (User user:users) {
            if (login.getId().equalsIgnoreCase(user.getId())) {
                signal = -1;
                if (login.getPw().equals(user.getPw())) {
                    if (user.getType() == User.Type.CLIENT) {
                        signal = 1; // Set as client
                    } else {
                        signal = 2; // Set as agent
                    }
                    break;
                }
            }
        }

        return signal;
    }
}
