package eu.siacs.conversations.cloud;

import java.util.List;

public class FederalInfo {

   public class State {
        private String no;
        private String name;
        private String xmppHost;

        public String getNo() {
            return no;
        }

        public void setNo(String no) {
            this.no = no;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getXmppHost() {
            return xmppHost;
        }

        public void setXmppHost(String xmppHost) {
            this.xmppHost = xmppHost;
        }
    }

    List<State> states;

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}
