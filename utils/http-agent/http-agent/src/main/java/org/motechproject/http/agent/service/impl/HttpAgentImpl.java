package org.motechproject.http.agent.service.impl;

import org.motechproject.event.MotechEvent;
import org.motechproject.http.agent.components.AsynchronousCall;
import org.motechproject.http.agent.components.SynchronousCall;
import org.motechproject.http.agent.domain.EventDataKeys;
import org.motechproject.http.agent.domain.EventSubjects;
import org.motechproject.http.agent.domain.Method;
import org.motechproject.http.agent.service.HttpAgent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class HttpAgentImpl implements HttpAgent {

    private AsynchronousCall asynchronousCall;
    private SynchronousCall synchronousCall;

    @Autowired
    public HttpAgentImpl(AsynchronousCall asynchronousCall, SynchronousCall synchronousCall) {
        this.asynchronousCall = asynchronousCall;
        this.synchronousCall = synchronousCall;
    }

    @Override
    public void execute(String url, Object data, Method method) {
        Map<String, Object> parameters = constructParametersFrom(url, data, method);
        sendMessage(parameters);
    }

    @Override
    public void executeSync(String url, Object data, Method method) {
        Map<String, Object> parameters = constructParametersFrom(url, data, method);
        sendMessageSync(parameters);
    }


    private Map<String, Object> constructParametersFrom(String url, Object data, Method method) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put(EventDataKeys.URL, url);
        parameters.put(EventDataKeys.METHOD, method);
        parameters.put(EventDataKeys.DATA, data);
        return parameters;
    }

    private void sendMessage(Map<String, Object> parameters) {
        MotechEvent motechEvent = new MotechEvent(EventSubjects.HTTP_REQUEST, parameters);
        asynchronousCall.send(motechEvent);
    }

    private void sendMessageSync(Map<String, Object> parameters) {
        MotechEvent motechEvent = new MotechEvent(EventSubjects.HTTP_REQUEST, parameters);
        synchronousCall.send(motechEvent);
    }
}