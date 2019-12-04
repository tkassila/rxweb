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
        String tmp_pattern = pattern;
        String strSearch = "glob:";
        if (tmp_pattern.startsWith(strSearch))
            tmp_pattern = tmp_pattern.substring(strSearch.length(), tmp_pattern.length());
        m_pattern = tmp_pattern;
        m_matcher = matcher;
    }

    @Override
    public boolean matches(Path path)
    {
        String tmpfpath = normalizeRegexMetaCharacters(path.toFile().getAbsolutePath());
        return m_matcher.match(this.m_pattern, tmpfpath);
    }

    public String normalizeRegexMetaCharacters(String str)
    {
        if (str == null)
            return null;
        if (str.trim().length() == 0)
            return str;
        String newstr = str.toString();
        String metachars = "<([{\\^-=$!|]})?*+.>";
        char [] mchar = metachars.toCharArray();
        int max = mchar.length;
        for(int i = 0; i < max; i++) {
            newstr = newstr.replace(""+mchar[i], ""+"\\" +mchar[i]);
        }
        return newstr;
    }
}
