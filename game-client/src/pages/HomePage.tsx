import { useNavigate } from "react-router-dom";
import { DropdownMenu } from "radix-ui";
import { useAppDispatch, useAppSelector } from "../app/hooks";
import { authApi, useLogoutMutation } from "../features/auth/authApi";

export function HomePage() {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const { status, session } = useAppSelector((state) => state.auth)
    const [logout] = useLogoutMutation()

    async function handleLogout() {
        try {
            await logout().unwrap()
        } catch {
            console.log("Cannot logout, check connection")
        } finally {
            dispatch(authApi.util.resetApiState());
            navigate("/", {replace: true})
        }
    }

    return (
    <>
        <h1>
            Home
        </h1>
        <h2>
            Welcome !
        </h2>

        {(status !== "authenticated" || !session) ? (
            <button type="button" onClick={() => navigate("/login")}>
                LOGIN
            </button>
        ) : (
            <DropdownMenu.Root>
                <DropdownMenu.Trigger asChild>
                    <button type="button" aria-label="Open account menu" >
                        Account: {session.user.displayName}
                    </button>
                </DropdownMenu.Trigger>

                <DropdownMenu.Portal>
                    <DropdownMenu.Content>
                        <DropdownMenu.Label>
                            LabelName
                        </DropdownMenu.Label>

                        <DropdownMenu.Group>
                            <DropdownMenu.Item onSelect={() => navigate("/lobby")}>
                                Lobby
                            </DropdownMenu.Item>

                            <DropdownMenu.Item onSelect={() => navigate("/profile")}>
                                Profile
                            </DropdownMenu.Item>
                        </DropdownMenu.Group>

                        <DropdownMenu.Separator/>

                        <DropdownMenu.Item onSelect={handleLogout}>
                            Sign out
                        </DropdownMenu.Item>
                    </DropdownMenu.Content>
                </DropdownMenu.Portal>
            </DropdownMenu.Root>
        )}

        <div>
            <p>
                Description about the web...
            </p>
        </div>
    </>
    )
}