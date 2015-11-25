package ru.intertrust.performance.jmetertools;

import org.apache.jmeter.protocol.http.sampler.HTTPSampleResult;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.visualizers.StatVisualizer;

public class GwtRpcStatVisualizer extends StatVisualizer {
    private static final long serialVersionUID = 6972136025433595525L;

    @Override
    public void add(final SampleResult res) {
        if (!(res instanceof HTTPSampleResult)){
            super.add(res);
        }
    }
}
