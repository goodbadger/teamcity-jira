package com.otr;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.BasePropertiesBean;
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm;
import jetbrains.buildServer.vcs.VcsRoot;
import jetbrains.buildServer.vcs.VcsRootEntry;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author pobedenniy.alexey
 * @since 26.01.2015
 */
public class PublishStatusController extends BaseController {

	private final String myUrl;
	private final PluginDescriptor myDescriptor;
	private final PublisherStatusManager myPublisherManager;
	private final PublishStatusSettingsController myPublisherSettingsController;
	private final PublishStatusSettings publisherSettings;

	public PublishStatusController(@NotNull WebControllerManager controllerManager,
	                               @NotNull PluginDescriptor descriptor,
	                               @NotNull PublisherStatusManager publisherManager,
	                               @NotNull PublishStatusSettingsController publisherSettingsController,
	                               @NotNull PublishStatusSettings publisherSettings) {
		myDescriptor = descriptor;
		myPublisherManager = publisherManager;
		myPublisherSettingsController = publisherSettingsController;
		this.publisherSettings = publisherSettings;
		myUrl = descriptor.getPluginResourcesPath("publishStatusFeature.html");
		controllerManager.registerController(myUrl, this);
	}

	public String getUrl() {
		return myUrl;
	}

	@Nullable
	@Override
	protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
		BasePropertiesBean props = (BasePropertiesBean) request.getAttribute("propertiesBean");
		String publisherId = props.getProperties().get("publisherId");
		ModelAndView mv = publisherId != null ? createEditPublisherModel(publisherId) : createAddPublisherModel();
		mv.addObject("publisherSettingsUrl", myPublisherSettingsController.getUrl());
		mv.addObject("vcsRoots", getVcsRoots(request));
		mv.addObject("projectId", getProjectId(request));
		return mv;
	}

	@NotNull
	private ModelAndView createAddPublisherModel() {
		ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("addPublisher.jsp"));
		mv.addObject("publishers", getPublisherSettings(true));
		return mv;
	}

	@NotNull
	private ModelAndView createEditPublisherModel(@NotNull String publisherId) {
		ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("editPublisher.jsp"));
		mv.addObject("publishers", getPublisherSettings(false));
		PublishStatusSettings publisherSettings = myPublisherManager.findSettings(publisherId);
		if (publisherSettings != null) {
			mv.addObject("editedPublisherUrl", publisherSettings.getEditSettingsUrl());
		}
		return mv;
	}

	@NotNull
	private List<VcsRoot> getVcsRoots(@NotNull HttpServletRequest request) {
		List<VcsRoot> roots = new ArrayList<VcsRoot>();
		BuildTypeForm buildTypeForm = (BuildTypeForm) request.getAttribute("buildForm");
		for (VcsRootEntry entry : buildTypeForm.getVcsRootsBean().getVcsRoots()) {
			roots.add(entry.getVcsRoot());
		}
		return roots;
	}

	@NotNull
	private String getProjectId(@NotNull HttpServletRequest request) {
		BuildTypeForm buildTypeForm = (BuildTypeForm) request.getAttribute("buildForm");
		return buildTypeForm.getProject().getExternalId();
	}

	private List<PublishStatusSettings> getPublisherSettings(boolean newPublisher) {
		List<PublishStatusSettings> publishers = new ArrayList<PublishStatusSettings>(myPublisherManager.getAllPublisherSettings());
		if (newPublisher) {
			final Map<String,String> map = Collections.emptyMap();
				publishers.add(0, publisherSettings);
		}
		return publishers;
	}
}
