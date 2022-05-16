package ru.skillbranch.skillarticles.ui.auth

import android.text.Spannable
import androidx.core.text.set
import androidx.navigation.navGraphViewModels
import ru.skillbranch.skillarticles.R
import ru.skillbranch.skillarticles.databinding.FragmentAuthBinding
import ru.skillbranch.skillarticles.extensions.attrValue
import ru.skillbranch.skillarticles.ui.BaseFragment
import ru.skillbranch.skillarticles.ui.custom.spans.UnderlineSpan
import ru.skillbranch.skillarticles.ui.delegates.viewBinding
import ru.skillbranch.skillarticles.viewmodels.auth.AuthState
import ru.skillbranch.skillarticles.viewmodels.auth.AuthViewModel

class AuthFragment() :
    BaseFragment<AuthState, AuthViewModel, FragmentAuthBinding>(R.layout.fragment_auth), IAuthView {


    override val viewModel: AuthViewModel by navGraphViewModels(R.id.auth_flow)
    override val viewBinding: FragmentAuthBinding by viewBinding(FragmentAuthBinding::bind)

    override fun renderUi(data: AuthState) {
        // handle input errors
    }

    override fun setupViews() {
        val decorColor = requireContext().attrValue(R.attr.colorPrimary)
        with(viewBinding) {
            tvPrivacy.setOnClickListener { onClickPrivacy() }
            tvRegister.setOnClickListener { onClickRegistration() }
            btnLogin.setOnClickListener { onClickLogin() }

            (tvPrivacy.text as Spannable).let { it[0..it.length] = UnderlineSpan(decorColor) }
            (tvRegister.text as Spannable).let { it[0..it.length] = UnderlineSpan(decorColor) }
        }
    }

    override fun onClickPrivacy() {
        viewModel.navigateToPrivacy()
    }

    override fun onClickRegistration() {
        viewModel.navigateToRegistration()
    }

    override fun onClickLogin() {
        viewModel.handleLogin(
            viewBinding.etLogin.text.toString(),
            viewBinding.etPassword.text.toString()
        )
    }


}