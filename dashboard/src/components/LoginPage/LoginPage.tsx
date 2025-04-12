import image from "../../assets/login-image.webp";
import registerTutorialImage from "../../assets/register_tutorial.webp";
import { Button, Description, Dialog, DialogPanel, DialogTitle, Input } from "@headlessui/react";
import { FormEvent, useState } from "react";
import { AnimatePresence, motion } from "framer-motion";
import { login } from "../../api/user.ts";
import { isAxiosError } from "axios";
import { setAuth } from "../../store/slices/authSlice.ts";
import { useNavigate } from "react-router-dom";
import { useDispatch } from "react-redux";

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
          tokenExpiry: response.data.expire,
        }));
        navigate("/");
      }
    } catch (err: any) {
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
                className="alert alert-error shadow-sm text-sm"
                initial={{ opacity: 0, y: -10 }}
                animate={{ opacity: 1, y: 0 }}
                exit={{ opacity: 0, y: -10 }}
                transition={{ duration: 0.2 }}
              >
                {error}
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
                {loading && <span className="loading loading-spinner"></span>}
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

      <AnimatePresence>
        {registerDialogState && (
          <Dialog static open={registerDialogState} onClose={() => setRegisterDialogState(false)}
                  className="relative z-50">
            <motion.div
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="fixed inset-0 bg-black/30"
            />
            <div className="fixed inset-0 flex w-screen items-center justify-center p-4 backdrop-blur-xl">
              <DialogPanel
                as={motion.div}
                initial={{ opacity: 0, scale: 0.95 }}
                animate={{ opacity: 1, scale: 1 }}
                exit={{ opacity: 0, scale: 0.95 }}
                className="max-w-lg space-y-4 bg-[#222] rounded-xl p-12"
              >
                <DialogTitle className="text-lg font-bold">Register a LunarCN Account</DialogTitle>
                <Description>Type <strong className="text-red-500">.passwd &lt;your_password&gt;</strong> in the bot to
                  set a password!</Description>
                <p className="text-red-400">DO NOT INPUT YOUR MINECRAFT PASSWORD.</p>
                <img src={registerTutorialImage} className="rounded-xl" alt="register tutorial" />
                <div className="flex gap-4">
                  <button onClick={() => setRegisterDialogState(false)} className="btn btn-soft btn-primary">I know,
                    thanks
                  </button>
                </div>
              </DialogPanel>
            </div>
          </Dialog>
        )}
      </AnimatePresence>
    </div>
  );
};

export default LoginPage;