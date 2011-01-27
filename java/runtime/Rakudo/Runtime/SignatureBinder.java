package Rakudo.Runtime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.P6capture;
import Rakudo.Metamodel.Representations.P6list;
import Rakudo.Metamodel.Representations.P6mapping;
import Rakudo.Runtime.Context;



/// <summary>
/// Simple signature binder implementation.
/// </summary>
public class SignatureBinder // C# has public static class
{
    /// <summary>
    /// Singleton empty positionals array.
    /// </summary>
    private static RakudoObject[] EmptyPos = new RakudoObject[0];

    /// <summary>
    /// Single empty nameds hash.
    /// </summary>
    private static HashMap<String, RakudoObject> EmptyNamed = new HashMap<String, RakudoObject>();

    /// <summary>
    /// Binds the capture against the given signature and stores the
    /// bound values into variables in the lexpad.
    /// 
    /// XXX No type-checking is available just yet. :-(
    /// 
    /// XXX No support for nameds mapping to positionals yet either.
    /// 
    /// (In other words, this kinda sucks...)
    /// </summary>
    /// <param name="C"></param>
    /// <param name="Capture"></param>
    public static void Bind(ThreadContext tc, Context c, RakudoObject capture)
// DROP    public static void Bind(Context tc, RakudoObject capture)
    {
        // Make sure the object is really a low level capture (don't handle
        // otherwise yet) and grab the pieces.
        P6capture.Instance nativeCapture = (P6capture.Instance)capture;
        if (nativeCapture == null)
            throw new UnsupportedOperationException("Can only deal with native captures at the moment");
        RakudoObject[] positionals = nativeCapture.Positionals != null ? nativeCapture.Positionals : EmptyPos;
        HashMap<String,RakudoObject> nameds = nativeCapture.Nameds != null ? nativeCapture.Nameds : EmptyNamed;
        HashMap<String, Boolean> seenNames = null; // C# has <string, bool> (a value type)

        // See if we have to do any flattening.
// TODO if (NativeCapture.FlattenSpec != null)
//          Flatten(nativeCapture.FlattenSpec, positionals, nameds); // C# has ref Positionals, ref Nameds :( http://javadude.com/articles/passbyvalue.htm and http://genamics.com/developer/csharp_comparative_part9.htm

        // If we have no signature, that's same as an empty signature.
        Signature sig = c.StaticCodeObject.Sig;
        if (sig == null)
            return;

        // Current positional.
        int curPositional = 0;

        // Iterate over the parameters.
        Parameter[] params = sig.Parameters;
        int numParams = params.length;
        for (int i = 0; i < numParams; i++)
        {
            Parameter param = params[i];

            // Positional required?
            if (param.Flags == Parameter.POS_FLAG)
            {
                if (curPositional < positionals.length)
                {
                    // We have an argument, just bind it.
                    c.LexPad.Storage[param.VariableLexpadPosition] = positionals[curPositional];
                }
                else
                {
                    throw new UnsupportedOperationException("Not enough positional parameters; got " +
                        Integer.toString(curPositional) + " but needed " +
                        Integer.toString(NumRequiredPositionals(c.StaticCodeObject.Sig)));
                }

                // Increment positional counter.
                curPositional++;
            }

            // Positional optional?
            else if (param.Flags == Parameter.OPTIONAL_FLAG)
            {
                if (curPositional < positionals.length)
                {
                    // We have an argument, just bind it.
                    c.LexPad.Storage[param.VariableLexpadPosition] = positionals[curPositional];
                    curPositional++;
                }
                else
                {
                    // Default value, vivification.
                    // ((RakudoCodeRef.Instance)Param.DefaultValue).CurrentContext = TC.CurrentContext;
                    c.LexPad.Storage[param.VariableLexpadPosition] = param.DefaultValue.getSTable().Invoke(tc, param.DefaultValue, capture);
                }
            }

            // Named slurpy?
            else if ((param.Flags & Parameter.NAMED_SLURPY_FLAG) != 0)
            {
                RakudoObject slurpyHolder = tc.DefaultHashType.getSTable().REPR.instance_of(tc, tc.DefaultHashType);
                c.LexPad.Storage[param.VariableLexpadPosition] = slurpyHolder;
                for (String name : nameds.keySet().toArray(new String[0]))
                    if (seenNames == null || !seenNames.containsKey(name))
                        Ops.llmapping_bind_at_key(tc, slurpyHolder,
                            Ops.box_str(tc, name, tc.DefaultStrBoxType),
                            nameds.get(name));
            }

            // Positional slurpy?
            else if ((param.Flags & Parameter.POS_SLURPY_FLAG) != 0)
            {
                RakudoObject slurpyHolder = tc.DefaultArrayType.getSTable().REPR.instance_of(tc, tc.DefaultArrayType);
                c.LexPad.Storage[param.VariableLexpadPosition] = slurpyHolder;
                int j;
                for (j = curPositional; j < positionals.length; j++)
                    Ops.lllist_push(tc, slurpyHolder, positionals[j]);
                curPositional = j;
            }

            // Named?
            else if (param.Name != null)
            {
                // Yes, try and get argument.
                if (nameds.containsKey(param.Name))
                {
                    // We have an argument, just bind it.
                    RakudoObject value = nameds.get(param.Name);
                    c.LexPad.Storage[param.VariableLexpadPosition] = value;
                    if (seenNames == null)
                        seenNames = new HashMap<String, Boolean>();
                    seenNames.put(param.Name, true);
                }
                else
                {
                    // Optional?
                    if ((param.Flags & Parameter.OPTIONAL_FLAG) == 0)
                    {
                        throw new UnsupportedOperationException("Required named parameter " + param.Name + " missing");
                    }
                    else
                    {
                        c.LexPad.Storage[param.VariableLexpadPosition] = param.DefaultValue.getSTable().Invoke(tc, param.DefaultValue, capture);
                    }
                }
            }

            // Otherwise, WTF?
            else
            {

            }
        }

        // Ensure we had enough positionals.
        int possiesInCapture = positionals.length;
        if (curPositional != possiesInCapture)
            throw new UnsupportedOperationException("Too many positional arguments passed; expected " +
                Integer.toString(NumRequiredPositionals(c.StaticCodeObject.Sig)) +
                " but got " + Integer.toString(possiesInCapture));

        // XXX TODO; Ensure we don't have leftover nameds.
    }

    /// <summary>
    /// The number of positionals we require.
    /// </summary>
    /// <param name="Sig"></param>
    /// <returns></returns>
    private static int NumRequiredPositionals(Signature sig)
    {
        int num = 0;
        for (Parameter param : sig.Parameters)
            if (param.Flags != 0 || param.Name != null)
                break;
            else
                num++;
        return num;
    }

    /// <summary>
    /// Flattens arguments into the positionals list or the nameds. This
    /// is pretty straightforward, could be optimized in various ways,
    /// such as special case where we only have one arg which is the whole
    /// parameter set.
    /// </summary>
    /// <param name="FlattenSpec"></param>
    /// <param name="Positionals"></param>
    /// <param name="Naneds"></param>
// TODO  private static void Flatten(int[] FlattenSpec, ref RakudoObject[] Positionals, ref Dictionary<string, RakudoObject> Naneds)
    private static void Flatten(int[] FlattenSpec, RakudoObject[] Positionals, HashMap<String, RakudoObject> Naneds)
    {
        // We'll build new positional and nameds.
        ArrayList<RakudoObject> NewPositionals = new ArrayList<RakudoObject>();
        HashMap<String, RakudoObject> NewNameds = new HashMap<String, RakudoObject>(Naneds);

        // Go through positional arguments and look for things to flatten.
        for (int i = 0; i < Positionals.length; i++)
        {
            if (FlattenSpec[i] == CaptureHelper.FLATTEN_NONE)
            {
                NewPositionals.add(Positionals[i]);
            }
            else if (FlattenSpec[i] == CaptureHelper.FLATTEN_POS)
            {
                // XXX For now rely on it being a P6list but in the future we
                // should handle other cases.
                P6list.Instance Flattenee = (P6list.Instance)Positionals[i];
                if (Flattenee != null)
                {
                    NewPositionals.addAll(Flattenee.Storage);
                }
                else
                {
                    throw new UnsupportedOperationException("Currently can only flatten a P6list");
                }
            }
            else if (FlattenSpec[i] == CaptureHelper.FLATTEN_NAMED)
            {
                // XXX For now rely on it being a P6mapping but in the future we
                // should handle other cases.
                P6mapping.Instance Flattenee = (P6mapping.Instance)Positionals[i];
                if (Flattenee != null)
                {
                    for (Map.Entry entryPair : Flattenee.Storage.entrySet()) // HashMap<String,RakudoObject> 
                        NewNameds.put((String)entryPair.getKey(), (RakudoObject)entryPair.getValue());
                }
                else
                {
                    throw new UnsupportedOperationException("Currently can only flatten a P6mapping");
                }
            }
        }

        // Put updated positionals and nameds in place.
        Positionals = (RakudoObject[])NewPositionals.toArray();
        Naneds = NewNameds;
    }
}
