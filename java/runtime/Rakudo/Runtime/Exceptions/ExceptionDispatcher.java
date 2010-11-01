package Rakudo.Runtime.Exceptions;

import Rakudo.Metamodel.Hints;
import Rakudo.Metamodel.RakudoObject;
import Rakudo.Metamodel.Representations.RakudoCodeRef;
import Rakudo.Runtime.CaptureHelper;
import Rakudo.Runtime.Exceptions.LeaveStackUnwinderException;
import Rakudo.Runtime.ThreadContext;
import Rakudo.Runtime.Ops;

/// <summary>
/// Various bits of logic relating to exception dispatch.
/// </summary>
public class ExceptionDispatcher  // public static in the C# version
{
    /// <summary>
    /// Invokes the specified exception handler with the given exception
    /// object.
    /// </summary>
    /// <param name="Handler"></param>
    /// <param name="ExceptionObject"></param>
    /// <returns></returns>
    public static RakudoObject CallHandler(ThreadContext TC, RakudoObject Handler, RakudoObject ExceptionObject)
    {
        // Invoke the handler. Note that in some cases we never return from it;
        // for example, the return exception handler does .leave.
        RakudoObject Returned = Handler.getSTable().Invoke.Invoke(TC, Handler, CaptureHelper.FormWith(new RakudoObject[] { ExceptionObject }));

        // So, we returned. Let's see if it's resumable.
        RakudoObject ResumableMeth = Returned.getSTable().FindMethod.FindMethod(TC, Returned, "resumable", Hints.NO_HINT);
        RakudoObject Resumable = ResumableMeth.getSTable().Invoke.Invoke(TC, ResumableMeth, CaptureHelper.FormWith(new RakudoObject[] { Returned }));
        if (Ops.unbox_int(TC, Resumable) != 0)
        {
            // Resumable, so don't need to stack unwind. Simply return
            // from here.
            return Returned;
        }
        else
        {
            // Not resumable, so stack unwind out of the block containing
            // the handler.
            throw new LeaveStackUnwinderException(
                ((RakudoCodeRef.Instance)Handler).OuterBlock,
                Returned);
        }
    }

    /// <summary>
    /// Dies from an unhandled exception.
    /// </summary>
    /// <param name="Exception"></param>
    public static void DieFromUnhandledException(ThreadContext TC, RakudoObject Exception)
    {
        // Try to stringify the exception object.
        try
        {
            RakudoObject StrMeth = Exception.getSTable().FindMethod.FindMethod(TC, Exception, "Str", Hints.NO_HINT);
            RakudoObject Stringified = StrMeth.getSTable().Invoke.Invoke(TC, StrMeth, CaptureHelper.FormWith(new RakudoObject[] { Exception }));
            System.out.println(Ops.unbox_str(TC, Stringified));
        }
        catch ( Exception ex )
        {
            System.err.println("Died from an exception, and died trying to stringify it too.");
        }

        // Exit with an error code.
        System.exit(1);
    }
}
