package org.dominokit.jacksonapt.deser.array.cast;

import elemental2.core.JsArray;
import elemental2.core.JsNumber;
import jsinterop.base.Js;
import org.dominokit.jacksonapt.JacksonContext;
import org.dominokit.jacksonapt.stream.JsonReader;

/**
 * <p>JsShortArrayReader class.</p>
 *
 * @author vegegoku
 * @version $Id: $Id
 */
public class JsShortArrayReader extends BaseJsNumberArrayReader implements JacksonContext.ShortArrayReader {
    /** {@inheritDoc} */
    @Override
    public short[] readArray(JsonReader reader) {
        return reinterpretCast(super.readNumberArray(reader));
    }

    private static short[] reinterpretCast(JsArray<JsNumber> value) {
        JsNumber[] sliced = value.slice();
        return Js.uncheckedCast(sliced);
    }
}
