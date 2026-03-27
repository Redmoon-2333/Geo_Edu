package com.geoedu;

import com.geoedu.mapper.KnowledgeMapper;
import com.geoedu.model.dto.KnowledgeCreateRequest;
import com.geoedu.model.dto.KnowledgeDTO;
import com.geoedu.model.dto.KnowledgeUpdateRequest;
import com.geoedu.model.dto.PageResponse;
import com.geoedu.model.entity.Knowledge;
import com.geoedu.service.KnowledgeService;
import com.geoedu.service.VectorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnowledgeServiceTest {

    @Mock
    private KnowledgeMapper knowledgeMapper;

    @Mock
    private VectorService vectorService;

    @InjectMocks
    private KnowledgeService knowledgeService;

    private Knowledge sampleKnowledge;
    private KnowledgeCreateRequest createRequest;

    @BeforeEach
    void setUp() {
        sampleKnowledge = Knowledge.builder()
                .id("1")
                .title("地球自转")
                .content("地球绕地轴自西向东转动")
                .difficulty("medium")
                .grade("grade1")
                .chapter("chapter1")
                .pageNumber(10)
                .build();

        createRequest = KnowledgeCreateRequest.builder()
                .title("地球自转")
                .content("地球绕地轴自西向东转动")
                .difficulty("medium")
                .grade("grade1")
                .chapter("chapter1")
                .pageNumber(10)
                .build();
    }

    @Test
    void createKnowledge_Success() {
        doNothing().when(knowledgeMapper).insert(any(Knowledge.class));
        when(vectorService.embedText(anyString())).thenReturn(new float[128]);

        KnowledgeDTO result = knowledgeService.create(createRequest);

        assertNotNull(result);
        assertEquals("地球自转", result.getTitle());
        assertEquals("地球绕地轴自西向东转动", result.getContent());
        assertEquals("medium", result.getDifficulty());
        verify(knowledgeMapper, times(1)).insert(any(Knowledge.class));
        verify(vectorService, times(1)).embedText(anyString());
        verify(vectorService, times(1)).saveVector(anyString(), any(float[].class), anyMap());
    }

    @Test
    void getById_Success() {
        when(knowledgeMapper.selectOneById("1")).thenReturn(sampleKnowledge);

        KnowledgeDTO result = knowledgeService.getById("1");

        assertNotNull(result);
        assertEquals("地球自转", result.getTitle());
        verify(knowledgeMapper, times(1)).selectOneById("1");
    }

    @Test
    void getById_NotFound() {
        when(knowledgeMapper.selectOneById("nonexistent")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> knowledgeService.getById("nonexistent"));
    }

    @Test
    void list_WithPagination() {
        List<Knowledge> knowledgeList = Arrays.asList(sampleKnowledge);
        when(knowledgeMapper.selectAll()).thenReturn(knowledgeList);

        PageResponse<KnowledgeDTO> result = knowledgeService.list(0, 10, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getItems().size());
        assertEquals("地球自转", result.getItems().get(0).getTitle());
    }

    @Test
    void updateKnowledge_Success() {
        KnowledgeUpdateRequest updateRequest = KnowledgeUpdateRequest.builder()
                .title("地球公转")
                .content("地球绕太阳转动")
                .build();

        when(knowledgeMapper.selectOneById("1")).thenReturn(sampleKnowledge);
        doNothing().when(knowledgeMapper).update(any(Knowledge.class));
        when(vectorService.embedText(anyString())).thenReturn(new float[128]);

        KnowledgeDTO result = knowledgeService.update("1", updateRequest);

        assertNotNull(result);
        verify(knowledgeMapper, times(1)).selectOneById("1");
        verify(knowledgeMapper, times(1)).update(any(Knowledge.class));
    }

    @Test
    void deleteKnowledge_Success() {
        when(knowledgeMapper.selectOneById("1")).thenReturn(sampleKnowledge);
        doNothing().when(knowledgeMapper).deleteById("1");

        knowledgeService.delete("1");

        verify(knowledgeMapper, times(1)).deleteById("1");
    }

    @Test
    void deleteKnowledge_NotFound() {
        when(knowledgeMapper.selectOneById("nonexistent")).thenReturn(null);

        assertThrows(RuntimeException.class, () -> knowledgeService.delete("nonexistent"));
    }
}
