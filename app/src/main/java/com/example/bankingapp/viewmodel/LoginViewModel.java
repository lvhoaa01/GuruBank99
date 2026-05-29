package com.example.bankingapp.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.bankingapp.model.User;
import com.example.bankingapp.model.enums.UserStatus;
import com.example.bankingapp.repository.UserRepository;
import com.example.bankingapp.utils.AppExecutors;
import com.example.bankingapp.utils.BusinessRules;
import com.example.bankingapp.utils.PasswordHasher;
import com.example.bankingapp.utils.SessionManager;
import com.example.bankingapp.utils.ValidationResult;
import com.example.bankingapp.utils.Validators;

/**
 * Login screen state.
 *
 * Validation is synchronous (pure functions, no DB). The DB-touching
 * branch — lookup by username, save the updated user — runs on the
 * io() executor, then posts the result via LiveData.postValue().
 */
public class LoginViewModel extends ViewModel {

    public static final int LOCKOUT_THRESHOLD = 3;
    public static final long LOCKOUT_WINDOW_MILLIS = 15L * 60L * 1000L;

    private final UserRepository users; // Dùng để truy cập dữ liệu người dùng trong quá trình đăng nhập
    private final SessionManager sessionManager; // Dùng để quản lý phiên đăng nhập của người dùng
    private final AppExecutors executors; // Dùng để thực thi các tác vụ không đồng bộ, như truy cập cơ sở dữ liệu, trên một luồng riêng biệt

    private final MutableLiveData<UiState> state = new MutableLiveData<>(UiState.idle()); 

    public LoginViewModel(UserRepository users, SessionManager sessionManager, AppExecutors executors) {
        this.users = users;
        this.sessionManager = sessionManager;
        this.executors = executors;
    }

    public LiveData<UiState> getState() {
        return state;
    }

    public void login(String username, String password) {
        login(username, password, System.currentTimeMillis()); 
    }

    /** Visible-for-testing variant with deterministic clock. */
    public void login(String username, String password, long nowMillis) {
        ValidationResult u = Validators.validateUsername(username);
        if (!u.isValid()) {
            state.setValue(UiState.error(u.getErrorCode(), u.getMessage()));
            return;
        }
        ValidationResult p = Validators.validatePasswordNotEmpty(password);
        if (!p.isValid()) {
            state.setValue(UiState.error(p.getErrorCode(), p.getMessage()));
            return;
        }

        state.setValue(UiState.loading());
        executors.io().execute(() -> {
            User user = users.findByUsername(username);
            String submittedHash = PasswordHasher.hash(password);
            BusinessRules.LoginResult result = BusinessRules.validateLogin(user, submittedHash, nowMillis);
            // BusinessRules được khởi tạo với một Clock giả định, nhưng giờ nó trả về LoginResult dựa trên logic nghiệp vụ, không cần biết thời gian thực là bao nhiêu. LoginViewModel chỉ cần truyền vào thời gian hiện tại để BusinessRules có thể tính toán lockout đúng cách. Điều này giúp tách biệt logic nghiệp vụ khỏi việc lấy thời gian thực tế, và cũng cho phép chúng ta kiểm thử logic lockout bằng cách giả lập thời gian trong các bài kiểm thử đơn vị.
            switch (result) {
                case USER_NOT_FOUND:
                    state.postValue(UiState.error("USER_NOT_FOUND", "User not found"));
                    return;
                case ACCOUNT_LOCKED:
                    state.postValue(UiState.error("ACCOUNT_LOCKED", "Account is temporarily locked. Try again later."));
                    return;
                case DISABLED:
                    state.postValue(UiState.error("DISABLED", "Account is disabled"));
                    return;
                case INVALID_CREDENTIALS:
                    handleFailure(user, nowMillis);
                    return;
                case SUCCESS:
                    user.setFailedLoginCount(0);
                    user.setLockedUntilMillis(0L);
                    users.save(user);
                    sessionManager.login(user.getCustomerId());
                    // Payload now carries the full User so the Activity can route
                    // by role (TELLER → TellerDashboard, CUSTOMER → Dashboard).
                    state.postValue(UiState.success("Login successful", user));
                    return;
                default:
                    state.postValue(UiState.error("UNKNOWN", "Unknown error"));
            }
        });
    }

    private void handleFailure(User user, long nowMillis) {
        int newCount = user.getFailedLoginCount() + 1;
        user.setFailedLoginCount(newCount);
        if (newCount >= LOCKOUT_THRESHOLD) {
            user.setStatus(UserStatus.LOCKED);
            user.setLockedUntilMillis(nowMillis + LOCKOUT_WINDOW_MILLIS);
            users.save(user);
            state.postValue(UiState.error("ACCOUNT_LOCKED",
                    "Account locked after 3 failed attempts. Try again in 15 minutes."));
            return;
        }
        users.save(user);
        state.postValue(UiState.error("INVALID_CREDENTIALS",
                "Invalid credentials. Attempts left: " + (LOCKOUT_THRESHOLD - newCount)));
    }
}
