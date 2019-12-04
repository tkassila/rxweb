package rxweb.support;

import java.nio.file.PathMatcher;
import java.nio.file.Path;

/**
 * This class implements java.nio.file.PathMatcher inferface. It is using AntPathMatcher
 * and String pattern from constructor.
 */
public class PathMatcherImpl implements PathMatcher {

    private final String m_pattern;
    private final AntPathMatcher m_matcher;

    public PathMatcherImpl(String pattern, AntPathMatcher matcher)
            throws NullPointerException
    {
        if (matcher == null)
            throw new NullPointerException("matcher param is null!");
        if (pattern == null)
            throw new NullPointerException("pattern param is null!");
        m_pattern = pattern;
        m_matcher = matcher;
    }

    @Override
    public boolean matches(Path path)
    {
        return m_matcher.match(this.m_pattern, path.toFile().getAbsolutePath());
    }
}
