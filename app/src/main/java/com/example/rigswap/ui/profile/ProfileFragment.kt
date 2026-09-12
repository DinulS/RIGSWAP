package com.example.rigswap.ui.profile

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rigswap.data.repository.RigswapRepository
import com.example.rigswap.databinding.FragmentProfileBinding
import com.example.rigswap.ui.auth.LoginActivity

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var repository: RigswapRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repository = RigswapRepository.getInstance(requireContext())

        com.example.rigswap.util.InsetUtils.applyTopSystemBarInsets(binding.topBar)

        val user = repository.getUser()
        binding.tvProfileUsername.text = user.username
        binding.tvProfileEmail.text = user.email

        binding.navSavedRigSpecs.setOnClickListener {
            val intent = Intent(requireContext(), SavedRigSpecsActivity::class.java)
            startActivity(intent)
        }

        binding.navCompatibleParts.setOnClickListener {
            val intent = Intent(requireContext(), CompatiblePartsActivity::class.java)
            startActivity(intent)
        }

        binding.navMerchantHub.setOnClickListener {
            val intent = Intent(requireContext(), MerchantHubActivity::class.java)
            startActivity(intent)
        }

        binding.navMyCart.setOnClickListener {
            val intent = Intent(requireContext(), CartActivity::class.java)
            startActivity(intent)
        }

        binding.btnSignOut.setOnClickListener {
            Toast.makeText(requireContext(), "Signed out successfully", Toast.LENGTH_SHORT).show()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.btnNotification.setOnClickListener {
            Toast.makeText(requireContext(), "No account notifications", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
