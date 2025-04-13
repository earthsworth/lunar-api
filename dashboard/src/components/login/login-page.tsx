import image from "../../assets/login-image.webp";
import registerTutorialImage from "../../assets/register_tutorial.webp";
import { FormEvent, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { login } from "@/api/user.ts";
import { isAxiosError } from "axios";
import { setAuth } from "@/store/slices/authSlice.ts";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";
import { Button } from "@/components/ui/button.tsx";
import { Input } from "@/components/ui/input.tsx";
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from "@/components/ui/dialog.tsx";
import { AlertCircle, Loader2 } from "lucide-react";
import { Alert, AlertDescription, AlertTitle } from "@/components/ui/alert.tsx";

const LoginPage = () => {
  const navigate = useNavigate();
  const dispatch = useDispatch();

  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const [registerDialogState, setRegisterDialogState] = useState(false);

  const handleClickRegister = () => {
    setRegisterDialogState(true); // open the dialog
  };

  const processLogin = async (e: FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    // process login
    try {
      const response = await login(username, password);
      if (response.code === 200 && response.data) {
        // store auth data
        dispatch(setAuth({
          token: response.data.token,
          tokenExpiry: response.data.expire
        }));
        navigate("/");
      }
    } catch (err) {
      if (isAxiosError(err)) {
        if (err.response) {
          setError(err.response.data.message);
        } else {
          setError(err.message);
        }
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex">
      <div className="hidden lg:flex w-1/2 items-center justify-center bg-base-200">
        <img
          src={image}
          alt="Image"
          className="w-full h-full object-cover"
        />
      </div>

      <div className="flex w-full lg:w-1/2 items-center justify-center px-8">
        <div className="w-full max-w-md space-y-6">
          <h1 className="text-3xl font-bold text-center select-none">The Lunar Dashboard</h1>

          <AnimatePresence>
            {error && (
              <motion.div
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                <Alert variant="destructive">
                  <AlertCircle className="h-4 w-4" />
                  <AlertTitle>Error</AlertTitle>
                  <AlertDescription>
                    {error}
                  </AlertDescription>
                </Alert>

              </motion.div>
            )}
          </AnimatePresence>

          <form className="space-y-4 w-full" onSubmit={processLogin}>
            <label className="floating-label">
              <span>Minecraft Username</span>
              <Input type="text" placeholder="Minecraft Username" pattern="^[a-zA-Z0-9_]{3,16}$"
                     className="input validator input-md w-full"
                     value={username}
                     onChange={(e) => setUsername(e.target.value)}
                     required
              />
            </label>

            <label className="floating-label">
              <span>Password</span>
              <Input type="password"
                     placeholder="Password"
                     className="input input-md w-full"
                     value={password}
                     onChange={(e) => setPassword(e.target.value)}
                     required
              />
            </label>

            <div className="form-control mt-6">
              <Button type="submit" className="btn btn-primary w-full" disabled={loading}>
                {loading && <Loader2 className="animate-spin" />}
                {loading ? "Please wait..." : "Login"}
              </Button>
            </div>
          </form>

          <div className="text-right select-none">
            <p className=" text-sm">
              Do not input your Microsoft password
            </p>
            <p className="text-blue-500 underline hover:text-sky-500 cursor-pointer"
               onClick={handleClickRegister}>Register</p>
          </div>
        </div>
      </div>

      <Dialog open={registerDialogState} onOpenChange={setRegisterDialogState}>
        <DialogContent className="max-w-lg rounded-xl p-12 space-y-4 backdrop-blur-xl">
          <DialogHeader>
            <DialogTitle className="text-lg font-bold">
              Register a LunarCN Account
            </DialogTitle>
            <DialogDescription>
              Type{" "}
              <strong className="text-red-500">
                .passwd &lt;your_password&gt;
              </strong>{" "}
              in the bot to set a password!
            </DialogDescription>
          </DialogHeader>

          <p className="text-red-400">DO NOT INPUT YOUR MINECRAFT PASSWORD.</p>

          <img
            src={registerTutorialImage}
            className="rounded-xl"
            alt="register tutorial"
          />

          <div className="flex gap-4">
            <Button onClick={() => setRegisterDialogState(false)}>
              I know, thanks
            </Button>
          </div>
        </DialogContent>
      </Dialog>
    </div>
  );
};

export default LoginPage;