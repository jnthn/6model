using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;
using Rakudo.Metamodel.Representations;

namespace Rakudo.Runtime.Exceptions
{
    /// <summary>
    /// Various bits of logic relating to exception dispatch.
    /// </summary>
    public static class ExceptionDispatcher
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
            RakudoObject Returned = Handler.STable.Invoke(TC, Handler, CaptureHelper.FormWith(new RakudoObject[] { ExceptionObject }));

            // So, we returned. Let's see if it's resumable.
            RakudoObject ResumableMeth = Returned.STable.FindMethod(TC, Returned, "resumable", Hints.NO_HINT);
            RakudoObject Resumable = ResumableMeth.STable.Invoke(TC, ResumableMeth, CaptureHelper.FormWith(new RakudoObject[] { Returned }));
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
                    (Handler as RakudoCodeRef.Instance).OuterBlock,
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
                RakudoObject StrMeth = Exception.STable.FindMethod(TC, Exception, "Str", Hints.NO_HINT);
                RakudoObject Stringified = StrMeth.STable.Invoke(TC, StrMeth, CaptureHelper.FormWith(new RakudoObject[] { Exception }));
                Console.WriteLine(Ops.unbox_str(TC, Stringified));
            }
            catch
            {
                Console.Error.WriteLine("Died from an exception, and died trying to stringify it too.");
            }

            // Exit with an error code.
            Environment.Exit(1);
        }
    }
}
