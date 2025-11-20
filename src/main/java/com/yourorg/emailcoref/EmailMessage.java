package com.yourorg.emailcoref;

public class EmailMessage {
    private final String messageId;
    private final String threadId;
    private final String fromName;
    private final String fromEmail;
    private final String subject;
    private final String body;

    public EmailMessage(String messageId,
                        String threadId,
                        String fromName,
                        String fromEmail,
                        String subject,
                        String body) {
        this.messageId = messageId;
        this.threadId = threadId;
        this.fromName = fromName;
        this.fromEmail = fromEmail;
        this.subject = subject;
        this.body = body;
    }

    public String getMessageId() { return messageId; }
    public String getThreadId() { return threadId; }
    public String getFromName() { return fromName; }
    public String getFromEmail() { return fromEmail; }
    public String getSubject() { return subject; }
    public String getBody() { return body; }
}
