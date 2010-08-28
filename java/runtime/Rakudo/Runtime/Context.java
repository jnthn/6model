package Rakudo.Runtime;

import java.util.HashMap;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;

/// <summary>
/// A context represents a given invocation of a block. (Note this is
/// fairly sketchy at the moment.)
/// </summary>
public class Context
{
    /// <summary>
    /// The static chain.
    /// </summary>
    public Context Outer;

    /// <summary>
    /// The dynamic chain.
    /// </summary>
    public Context Caller;

    /// <summary>
    /// The static code object.
    /// </summary>
    public RakudoCodeRef.Instance StaticCodeObject;

    /// <summary>
    /// Lexpad. Note that we'll in the end have something much smarter
    /// for this but it'll do for now.
    /// </summary>
    public HashMap<String, RakudoObject> LexPad;

    /// <summary>
    /// The capture passed as part of the current call.
    /// </summary>
    public RakudoObject Capture;

    /// <summary>
    /// Creates an empty, uninitialized context.
    /// </summary>
    public Context()
    {
    }

    /// <summary>
    /// Constructor initializes the context.
    /// </summary>
    /// <param name="StaticCodeObject"></param>
    /// <param name="Caller"></param>
    public Context(RakudoCodeRef.Instance staticCodeObject, Context caller, RakudoObject capture)
    {
        // Set up static code object and caller pointers.
        this.StaticCodeObject = staticCodeObject;
        this.Caller = caller;
        this.Capture = capture;

        // Static sub object should have this as the current
        // context.
        staticCodeObject.CurrentContext = this;

        // Lex pad should be copy of the static one.
        // XXX This isn't quite what we want in the long run, but it
        // does fine for now.
        this.LexPad = new HashMap<String, RakudoObject>();
//TODO  this.LexPad = new HashMap<String, RakudoObject>(staticCodeObject.StaticLexPad);

        // Set outer context.
        RakudoCodeRef.Instance outerBlock = staticCodeObject.OuterBlock;
        if (outerBlock.CurrentContext != null)
        {
            this.Outer = outerBlock.CurrentContext;
        }
        else
        {
            // Auto-close. In this we go setting up fake contexts
            // that use the static lexpad until we find a real one.
            Context curContext = this;
            while (outerBlock != null)
            {
                // If we found a block with a context, we're done.
                if (outerBlock.CurrentContext != null)
                {
                    curContext.Outer = outerBlock.CurrentContext;
                    break;
                }

                // Build the fake context.
                Context outerContext = new Context();
                outerContext.StaticCodeObject = outerBlock;
// TODO         outerContext.LexPad = outerBlock.StaticLexPad;

                // Link it.
                curContext.Outer = outerContext;

                // Step back one level.
                curContext = outerContext;
                outerBlock = outerBlock.OuterBlock;
            }
        }
    }
}

