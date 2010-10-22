using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using Rakudo.Metamodel;

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
            throw new NotImplementedException();
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
                var StrMeth = Exception.STable.FindMethod(TC, Exception, "Str", Hints.NO_HINT);
                var Stringified = StrMeth.STable.Invoke(TC, StrMeth, CaptureHelper.FormWith(new RakudoObject[] { Exception }));
                Console.WriteLine(Ops.unbox_str(TC, Stringified));
            }
            catch
            {
                Console.Error.WriteLine("Died from an exception, and died trying to stringify it too.");
            }

            // We'll also write a stack trace. It's a .Net one for now.
            Console.Error.Write(new Exception().StackTrace);

            // Exit with an error code.
            Environment.Exit(1);
        }
    }
}
