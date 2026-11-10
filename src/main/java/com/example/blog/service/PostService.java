package com.example.blog.service;

import com.example.blog.dto.PostRequest;
import com.example.blog.dto.PostResponse;
import com.example.blog.entity.Post;
import com.example.blog.entity.PostStatus;
import com.example.blog.entity.User;
import com.example.blog.exception.ResourceNotFoundException;
import com.example.blog.repository.CommentRepository;
import com.example.blog.repository.PostRepository;
import com.example.blog.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final CommentRepository commentRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,
                       CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.commentRepository = commentRepository;
    }

    public PostResponse create(PostRequest req, String username) {
        User author = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        Post post = new Post();
        post.setTitle(req.title());
        post.setContent(req.content());
        post.setStatus(req.status() != null ? req.status() : PostStatus.DRAFT);
        post.setSlug(uniqueSlug(req.title()));
        post.setAuthor(author);
        return PostResponse.from(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public List<PostResponse> getAll() {
        return postRepository.findAllByOrderByCreatedAtDesc()
                .stream().map(PostResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public PostResponse getById(Long id) {
        return PostResponse.from(find(id));
    }

    @Transactional(readOnly = true)
    public PostResponse getBySlug(String slug) {
        return postRepository.findBySlug(slug)
                .map(PostResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with slug: " + slug));
    }

    public PostResponse update(Long id, PostRequest req, Authentication auth) {
        Post post = find(id);
        checkOwner(post, auth);
        post.setTitle(req.title());
        post.setContent(req.content());
        if (req.status() != null) {
            post.setStatus(req.status());
        }
        return PostResponse.from(postRepository.save(post));
    }

    public void delete(Long id, Authentication auth) {
        Post post = find(id);
        checkOwner(post, auth);
        commentRepository.deleteByPostId(id);
        postRepository.delete(post);
    }

    private void checkOwner(Post post, Authentication auth) {
        boolean admin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        boolean owner = post.getAuthor() != null
                && post.getAuthor().getUsername().equals(auth.getName());
        if (!admin && !owner) {
            throw new AccessDeniedException("You can only modify your own posts");
        }
    }

    private Post find(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found with id: " + id));
    }

    private String uniqueSlug(String title) {
        String base = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");
        if (base.isEmpty()) {
            base = "post";
        }
        String slug = base;
        int n = 2;
        while (postRepository.existsBySlug(slug)) {
            slug = base + "-" + n++;
        }
        return slug;
    }
}
