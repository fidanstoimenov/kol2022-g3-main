package mk.ukim.finki.wp.kol2022.g3.service.impl;

import mk.ukim.finki.wp.kol2022.g3.model.ForumUser;
import mk.ukim.finki.wp.kol2022.g3.model.ForumUserType;
import mk.ukim.finki.wp.kol2022.g3.model.Interest;
import mk.ukim.finki.wp.kol2022.g3.model.exceptions.InvalidForumUserIdException;
import mk.ukim.finki.wp.kol2022.g3.repository.ForumUserRepository;
import mk.ukim.finki.wp.kol2022.g3.repository.InterestRepository;
import mk.ukim.finki.wp.kol2022.g3.service.ForumUserService;
import mk.ukim.finki.wp.kol2022.g3.service.InterestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ForumUserServiceImpl implements ForumUserService, UserDetailsService
{
    @Autowired
    private ForumUserRepository forumUserRepository;
    @Autowired
    private InterestRepository interestRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private InterestService interestService;

    @Override
    public List<ForumUser> listAll() {
        return forumUserRepository.findAll();
    }

    @Override
    public ForumUser findById(Long id) {
        return forumUserRepository.findById(id).orElseThrow(InvalidForumUserIdException::new);
    }

    @Override
    public ForumUser create(String name, String email, String password, ForumUserType type, List<Long> interestId, LocalDate birthday) {
        List<Interest> interestList = interestRepository.findAllById(interestId);
        ForumUser forumUser = new ForumUser(name,email,passwordEncoder.encode(password),type,interestList,birthday);
        return forumUserRepository.save(forumUser);
    }

    @Override
    public ForumUser update(Long id, String name, String email, String password, ForumUserType type, List<Long> interestId, LocalDate birthday) {
        List<Interest> interestList = interestRepository.findAllById(interestId);
        ForumUser forumUser = this.findById(id);

        forumUser.setName(name);
        forumUser.setEmail(email);
        forumUser.setPassword(passwordEncoder.encode(password));
        forumUser.setType(type);
        forumUser.setInterests(interestList);
        forumUser.setBirthday(birthday);

        return forumUserRepository.save(forumUser);
    }

    @Override
    public ForumUser delete(Long id) {
        ForumUser forumUser = this.findById(id);
        forumUserRepository.delete(forumUser);
        return forumUser;
    }

    @Override
    public List<ForumUser> filter(Long interestId, Integer age) {
        if(interestId==null && age==null)
        {
            return listAll();
        }
        else if(interestId==null)
        {
            return forumUserRepository.findAllByBirthdayBefore(LocalDate.now().minusYears(age));
        }
        else if(age==null)
        {
            return forumUserRepository.findAllByInterestsContaining(interestService.findById(interestId));
        }
        else
        {
            return forumUserRepository.findAllByInterestsContainingAndBirthdayBefore(interestService.findById(interestId),LocalDate.now().minusYears(age));
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        ForumUser forumUser = forumUserRepository.findByEmail(username);

        if(forumUser==null)
        {
            throw new UsernameNotFoundException(username);
        }

        return User.builder()
                .username(forumUser.getEmail())
                .password(forumUser.getPassword())
                .authorities("ROLE_"+forumUser.getType())
                .build();
    }
}
