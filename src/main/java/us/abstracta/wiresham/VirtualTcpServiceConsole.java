package us.abstracta.wiresham;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import javax.swing.JTextArea;

public class VirtualTcpServiceConsole extends AppenderBase<ILoggingEvent> {

  private JTextArea console;

  public VirtualTcpServiceConsole(JTextArea console) {
    this.console = console;
  }

  @Override
  protected void append(ILoggingEvent iLoggingEvent) {
    console.append(iLoggingEvent.getFormattedMessage());
    console.append("\n");
  }
}
