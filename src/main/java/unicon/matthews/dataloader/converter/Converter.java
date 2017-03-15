package unicon.matthews.dataloader.converter;

import unicon.matthews.caliper.Event;

/**
 * General interface for type converters. The Type parameters &lt;S, T &gt; will allow the converters to be type safe
 * and allow them to be grouped by type for distinct use in the conversion service, further filtered by the supports
 * method.
 */
public interface Converter<S, T> {

    boolean supports(S source);

    T convert(S source, SupportingEntities supportingEntities);

}
