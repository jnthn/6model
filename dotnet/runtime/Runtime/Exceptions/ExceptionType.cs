using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Rakudo.Runtime.Exceptions
{
    /// <summary>
    /// Various types of exceptions that can occur. (The constant values
    /// match those used in Parrot for the control exceptions. Maybe that'll
    /// be OK, maybe we should encapsulate things a bit more. We'll see.)
    /// </summary>
    public static class ExceptionType
    {
        public const int NORMAL            = 0;
        public const int CONTROL_RETURN    = 57;
        public const int CONTROL_OK        = 58;
        public const int CONTROL_BREAK     = 59;
        public const int CONTROL_CONTINUE  = 60;
        public const int CONTROL_ERROR     = 61;
        public const int CONTROL_TAKE      = 62;
        public const int CONTROL_LEAVE     = 63;
        public const int CONTROL_EXIT      = 64;
        public const int CONTROL_LOOP_NEXT = 65;
        public const int CONTROL_LOOP_LAST = 66;
        public const int CONTROL_LOOP_REDO = 67;
    }
}
