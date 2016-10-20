package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.P6capture;




/// <summary>
/// Provides helper methods for getting stuff into and out of captures,
/// both native ones and user-level ones.
/// </summary>
public class CaptureHelper   // C# has static class
{
    /// <summary>
    /// Don't flatten.
    /// </summary>
    public static final int FLATTEN_NONE = 0; // C# has public const

    /// <summary>
    /// Flatten positionally.
    /// </summary>
    public static final int FLATTEN_POS = 1;

    /// <summary>
    /// Flatten named.
    /// </summary>
    public static final int FLATTEN_NAMED = 2;

    /// <summary>
    /// Cache of the native capture type object.
    /// </summary>
    public static RakudoObject CaptureTypeObject;  // C# has internal

    /// <summary>
    /// Empty capture former.
    /// </summary>
    /// <returns></returns>
    public static RakudoObject FormWith()
    {
        P6capture.Instance capture = (P6capture.Instance)CaptureTypeObject.getSTable().REPR.instance_of(null, CaptureTypeObject);
        return capture;
    }

    /// <summary>
    /// Forms a capture from the provided positional arguments.
    /// </summary>
    /// <param name="Args"></param>
    /// <returns></returns>
    public static RakudoObject FormWith(RakudoObject[] posArgs)
    {
        P6capture.Instance capture = (P6capture.Instance)CaptureTypeObject.getSTable().REPR.instance_of(null, CaptureTypeObject);
        capture.Positionals = posArgs;
        return capture;
    }

    /// <summary>
    /// Forms a capture from the provided positional and named arguments.
    /// </summary>
    /// <param name="Args"></param>
    /// <returns></returns>
    public static RakudoObject FormWith(RakudoObject[] posArgs, HashMap<String, RakudoObject> namedArgs)
    {
        P6capture.Instance capture = (P6capture.Instance)CaptureTypeObject.getSTable().REPR.instance_of(null, CaptureTypeObject);
        capture.Positionals = posArgs;
        capture.Nameds = namedArgs;
        return capture;
    }

    /// <summary>
    /// Forms a capture from the provided positional and named arguments
    /// and the given flattening spec.
    /// </summary>
    /// <param name="PosArgs"></param>
    /// <param name="NamedArgs"></param>
    /// <param name="FlattenSpec"></param>
    /// <returns></returns>
    public static RakudoObject FormWith(RakudoObject[] posArgs, HashMap<String, RakudoObject> namedArgs, int[] flattenSpec)
    {
        P6capture.Instance capture = (P6capture.Instance)CaptureTypeObject.getSTable().REPR.instance_of(null, CaptureTypeObject);
        capture.Positionals = posArgs;
        capture.Nameds = namedArgs;
        capture.FlattenSpec = flattenSpec;
        return capture;
    }

    /// <summary>
    /// Get a positional argument from a capture.
    /// </summary>
    /// <param name="Capture"></param>
    /// <param name="Pos"></param>
    /// <returns></returns>
    public static RakudoObject GetPositional(RakudoObject capture, int pos)
    {
        P6capture.Instance nativeCapture = (P6capture.Instance)capture;
        if (nativeCapture != null)
        {
            RakudoObject[] possies = nativeCapture.Positionals;
            if (possies != null && pos < possies.length && pos >= 0)
                return possies[pos];
            else
                return null;
        }
        else
        {
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        }
    };

    /// <summary>
    /// Number of positionals.
    /// </summary>
    /// <param name="Capture"></param>
    /// <returns></returns>
    public static int NumPositionals(RakudoObject capture)
    {
        P6capture.Instance nativeCapture = (P6capture.Instance) capture;
        if (nativeCapture != null)
        {
            RakudoObject[] possies = nativeCapture.Positionals;
            return possies == null ? 0 : possies.length;
        }
        else
        {
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        }
    }

    /// <summary>
    /// Get a named argument from a capture.
    /// </summary>
    /// <param name="Capture"></param>
    /// <param name="Pos"></param>
    /// <returns></returns>
    public static RakudoObject GetNamed(RakudoObject capture, String name)
    {
        P6capture.Instance nativeCapture = (P6capture.Instance)capture;
        if (nativeCapture != null)
        {
            HashMap<String,RakudoObject> nameds = nativeCapture.Nameds;
            if (nameds != null && nameds.containsKey(name))
                return nameds.get(name);
            else
                return null;
        }
        else
        {
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        }
    }

    /// <summary>
    /// Gets a positional and tries to unbox it to the given type.
    /// XXX In the future, we can make it call a coercion too, if
    /// needed.
    /// </summary>
    /// <param name="Capture"></param>
    /// <param name="Pos"></param>
    /// <returns></returns>
    public static String GetPositionalAsString(RakudoObject capture, int pos)
    {
        return Ops.unbox_str(null, GetPositional(capture, pos));
    }

    /// <summary>
    /// XXX This is very wrong...
    /// </summary>
    /// <returns></returns>
    public static RakudoObject Nil()
    {
        return null;
    }
}
